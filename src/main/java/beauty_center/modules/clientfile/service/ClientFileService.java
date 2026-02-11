package beauty_center.modules.clientfile.service;

import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.clientfile.dto.*;
import beauty_center.modules.clientfile.entity.ClientFile;
import beauty_center.modules.clientfile.repository.ClientFileRepository;
import beauty_center.modules.users.repository.UserAccountRepository;
import beauty_center.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Client file service managing sensitive medical and personal information.
 * Implements strict access control:
 * - Clients can read/write their own declarative sections
 * - Employees/Admins can read client files (read-only for declarative sections)
 * All operations are logged to audit trail.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientFileService {

    private final ClientFileRepository clientFileRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    /**
     * Get client file with access control verification.
     * Logs sensitive data read to audit trail.
     *
     * @param clientId UUID of the client
     * @return Optional containing ClientFile if found and accessible
     * @throws AccessDeniedException if current user doesn't have access
     */
    public Optional<ClientFile> getClientFile(UUID clientId) {
        // Verify access permissions
        verifyReadAccess(clientId);

        Optional<ClientFile> clientFile = clientFileRepository.findByClientId(clientId);

        // Log sensitive data read
        if (clientFile.isPresent()) {
            auditService.logSensitiveRead("ClientFile", clientFile.get().getId());
            log.info("Client file accessed: clientId={}, actorId={}", clientId, currentUser.getUserId());
        }

        return clientFile;
    }

    /**
     * Get current client's own file.
     * Creates file if it doesn't exist.
     *
     * @return ClientFile for current client
     */
    public ClientFile getMyClientFile() {
        UUID clientId = currentUser.getUserId();
        if (clientId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Verify user is a client
        if (!currentUser.hasRole("CLIENT")) {
            throw new AccessDeniedException("Only clients can access their own file");
        }

        Optional<ClientFile> existingFile = clientFileRepository.findByClientId(clientId);
        if (existingFile.isPresent()) {
            auditService.logSensitiveRead("ClientFile", existingFile.get().getId());
            return existingFile.get();
        }

        // Create new file if doesn't exist
        ClientFile newFile = ClientFile.builder()
                .clientId(clientId)
                .photoConsentForFollowup(false)
                .photoConsentForMarketing(false)
                .build();

        ClientFile saved = clientFileRepository.save(newFile);
        auditService.logCreate("ClientFile", saved.getId(), saved);
        log.info("Client file created: clientId={}, fileId={}", clientId, saved.getId());

        return saved;
    }

    /**
     * Update client file declarative sections.
     * Only the client who owns the file can update it.
     *
     * @param clientId UUID of the client
     * @param request  Update request with new data
     * @return Updated ClientFile
     * @throws AccessDeniedException if current user is not the client
     */
    public ClientFile updateClientFile(UUID clientId, ClientFileUpdateRequest request) {
        // Verify current user is the client (only clients can update their own data)
        verifyWriteAccess(clientId);

        // Get existing file or create new one
        ClientFile existingFile = clientFileRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    ClientFile newFile = ClientFile.builder()
                            .clientId(clientId)
                            .photoConsentForFollowup(false)
                            .photoConsentForMarketing(false)
                            .build();
                    return clientFileRepository.save(newFile);
                });

        // Capture before state for audit
        ClientFile beforeState = cloneClientFile(existingFile);

        // Update fields from request
        if (request.getIntake() != null) {
            updateIntakeSection(existingFile, request.getIntake());
        }

        if (request.getMedicalHistory() != null) {
            updateMedicalHistorySection(existingFile, request.getMedicalHistory());
        }

        if (request.getAestheticProcedureHistory() != null) {
            updateAestheticProcedureHistorySection(existingFile, request.getAestheticProcedureHistory());
        }

        if (request.getPhotoConsentForFollowup() != null) {
            existingFile.setPhotoConsentForFollowup(request.getPhotoConsentForFollowup());
        }

        if (request.getPhotoConsentForMarketing() != null) {
            existingFile.setPhotoConsentForMarketing(request.getPhotoConsentForMarketing());
        }

        // Save updated file
        ClientFile updated = clientFileRepository.save(existingFile);

        // Log update operation
        auditService.logUpdate("ClientFile", updated.getId(), beforeState, updated);
        log.info("Client file updated: clientId={}, fileId={}, actorId={}", clientId, updated.getId(),
                currentUser.getUserId());

        return updated;
    }

    /**
     * Update current client's own file.
     *
     * @param request Update request with new data
     * @return Updated ClientFile
     */
    public ClientFile updateMyClientFile(ClientFileUpdateRequest request) {
        UUID clientId = currentUser.getUserId();
        if (clientId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!currentUser.hasRole("CLIENT")) {
            throw new AccessDeniedException("Only clients can update their own file");
        }

        return updateClientFile(clientId, request);
    }

    /**
     * Convert ClientFile entity to ClientFileResponse DTO.
     *
     * @param clientFile ClientFile entity
     * @return ClientFileResponse DTO
     */
    public ClientFileResponse toResponse(ClientFile clientFile) {
        if (clientFile == null) {
            return null;
        }

        return ClientFileResponse.builder()
                .id(clientFile.getId())
                .clientId(clientFile.getClientId())
                .intake(ClientIntakeDto.builder()
                        .howDidYouHearAboutUs(clientFile.getHowDidYouHearAboutUs())
                        .consultationReason(clientFile.getConsultationReason())
                        .objective(clientFile.getObjective())
                        .careType(clientFile.getCareType())
                        .skincareRoutine(clientFile.getSkincareRoutine())
                        .habits(clientFile.getHabits())
                        .build())
                .medicalHistory(MedicalHistoryDto.builder()
                        .medicalBackground(clientFile.getMedicalBackground())
                        .currentTreatments(clientFile.getCurrentTreatments())
                        .allergiesAndReactions(clientFile.getAllergiesAndReactions())
                        .build())
                .aestheticProcedureHistory(AestheticProcedureHistoryDto.builder()
                        .procedures(clientFile.getProcedures())
                        .build())
                .photoConsentForFollowup(clientFile.isPhotoConsentForFollowup())
                .photoConsentForMarketing(clientFile.isPhotoConsentForMarketing())
                .createdAt(clientFile.getCreatedAt())
                .updatedAt(clientFile.getUpdatedAt())
                .build();
    }

    // ===== Private Helper Methods =====

    /**
     * Verify current user has read access to client file.
     * Clients can read their own file, Employees/Admins can read any client file.
     */
    private void verifyReadAccess(UUID clientId) {
        UUID currentUserId = currentUser.getUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Client can read own file
        if (currentUserId.equals(clientId) && currentUser.hasRole("CLIENT")) {
            return;
        }

        // Employee or Admin can read any client file
        if (currentUser.hasAnyRole("EMPLOYEE", "ADMIN")) {
            return;
        }

        throw new AccessDeniedException("Access denied to client file: " + clientId);
    }

    /**
     * Verify current user has write access to client file.
     * Only the client who owns the file can update it.
     */
    private void verifyWriteAccess(UUID clientId) {
        UUID currentUserId = currentUser.getUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        // Only the client can update their own file
        if (!currentUserId.equals(clientId)) {
            throw new AccessDeniedException("Clients can only update their own file");
        }

        if (!currentUser.hasRole("CLIENT")) {
            throw new AccessDeniedException("Only clients can update their file");
        }
    }

    /**
     * Update intake section from DTO.
     */
    private void updateIntakeSection(ClientFile file, ClientIntakeDto dto) {
        if (dto.getHowDidYouHearAboutUs() != null) {
            file.setHowDidYouHearAboutUs(dto.getHowDidYouHearAboutUs());
        }
        if (dto.getConsultationReason() != null) {
            file.setConsultationReason(dto.getConsultationReason());
        }
        if (dto.getObjective() != null) {
            file.setObjective(dto.getObjective());
        }
        if (dto.getCareType() != null) {
            file.setCareType(dto.getCareType());
        }
        if (dto.getSkincareRoutine() != null) {
            file.setSkincareRoutine(dto.getSkincareRoutine());
        }
        if (dto.getHabits() != null) {
            file.setHabits(dto.getHabits());
        }
    }

    /**
     * Update medical history section from DTO.
     */
    private void updateMedicalHistorySection(ClientFile file, MedicalHistoryDto dto) {
        if (dto.getMedicalBackground() != null) {
            file.setMedicalBackground(dto.getMedicalBackground());
        }
        if (dto.getCurrentTreatments() != null) {
            file.setCurrentTreatments(dto.getCurrentTreatments());
        }
        if (dto.getAllergiesAndReactions() != null) {
            file.setAllergiesAndReactions(dto.getAllergiesAndReactions());
        }
    }

    /**
     * Update aesthetic procedure history section from DTO.
     */
    private void updateAestheticProcedureHistorySection(ClientFile file, AestheticProcedureHistoryDto dto) {
        if (dto.getProcedures() != null) {
            file.setProcedures(dto.getProcedures());
        }
    }

    /**
     * Clone ClientFile for audit trail (before state).
     */
    private ClientFile cloneClientFile(ClientFile original) {
        return ClientFile.builder()
                .id(original.getId())
                .clientId(original.getClientId())
                .howDidYouHearAboutUs(original.getHowDidYouHearAboutUs())
                .consultationReason(original.getConsultationReason())
                .objective(original.getObjective())
                .careType(original.getCareType())
                .skincareRoutine(original.getSkincareRoutine())
                .habits(original.getHabits())
                .medicalBackground(original.getMedicalBackground())
                .currentTreatments(original.getCurrentTreatments())
                .allergiesAndReactions(original.getAllergiesAndReactions())
                .procedures(original.getProcedures())
                .photoConsentForFollowup(original.isPhotoConsentForFollowup())
                .photoConsentForMarketing(original.isPhotoConsentForMarketing())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
    }
}

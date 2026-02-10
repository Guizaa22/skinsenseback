package beauty_center.modules.scheduling.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.scheduling.dto.AbsenceCreateRequest;
import beauty_center.modules.scheduling.dto.AbsenceResponse;
import beauty_center.modules.scheduling.entity.Absence;
import beauty_center.modules.scheduling.repository.AbsenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Absence service managing employee time off and vacations.
 * Handles validation for absence overlaps and appointment conflicts.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Get all absences for an employee
     */
    public List<AbsenceResponse> getEmployeeAbsences(UUID employeeId) {
        log.debug("Fetching absences for employee: {}", employeeId);
        List<Absence> absences = absenceRepository.findByEmployeeId(employeeId);
        return absences.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Create new absence for an employee
     */
    public AbsenceResponse createAbsence(UUID employeeId, AbsenceCreateRequest request) {
        log.info("Creating absence for employee: {} from {} to {}",
            employeeId, request.getStartAt(), request.getEndAt());

        // Validate absence dates
        validateAbsenceDates(request);

        // Check for overlapping absences
        validateNoOverlappingAbsences(employeeId, request, null);

        // Check for conflicting appointments
        checkAppointmentConflicts(employeeId, request);

        // Create absence
        Absence absence = Absence.builder()
            .employeeId(employeeId)
            .startAt(request.getStartAt())
            .endAt(request.getEndAt())
            .reason(request.getReason())
            .build();

        Absence saved = absenceRepository.save(absence);
        log.info("Absence created successfully: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Delete an absence
     */
    public void deleteAbsence(UUID absenceId) {
        log.info("Deleting absence: {}", absenceId);

        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new IllegalArgumentException("Absence not found: " + absenceId));

        absenceRepository.delete(absence);
        log.info("Absence deleted successfully: {}", absenceId);
    }

    /**
     * Validate absence dates are valid
     */
    private void validateAbsenceDates(AbsenceCreateRequest request) {
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new IllegalArgumentException(
                String.format("End date/time (%s) must be after start date/time (%s)",
                    request.getEndAt(), request.getStartAt())
            );
        }
    }

    /**
     * Validate no overlapping absences exist for the employee
     */
    private void validateNoOverlappingAbsences(
        UUID employeeId,
        AbsenceCreateRequest request,
        UUID excludeAbsenceId
    ) {
        UUID excludeId = excludeAbsenceId != null ? excludeAbsenceId : UUID.randomUUID();

        boolean hasOverlap = absenceRepository.existsOverlappingAbsence(
            employeeId,
            request.getStartAt(),
            request.getEndAt(),
            excludeId
        );

        if (hasOverlap) {
            throw new IllegalArgumentException(
                String.format("Employee already has an absence during this period: %s to %s",
                    request.getStartAt(), request.getEndAt())
            );
        }
    }

    /**
     * Check for conflicting appointments during the absence period.
     * MVP: Block absence creation if appointments exist.
     * Future: Could auto-cancel or require manual handling.
     */
    private void checkAppointmentConflicts(UUID employeeId, AbsenceCreateRequest request) {
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByEmployeeIdAndDateRange(
                employeeId,
                request.getStartAt(),
                request.getEndAt()
            )
            .stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
            .collect(Collectors.toList());

        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Cannot create absence: %d confirmed appointment(s) exist during this period. " +
                    "Please cancel or reschedule appointments first.",
                    conflictingAppointments.size())
            );
        }
    }

    /**
     * Map entity to response DTO
     */
    private AbsenceResponse mapToResponse(Absence absence) {
        return AbsenceResponse.builder()
            .id(absence.getId())
            .employeeId(absence.getEmployeeId())
            .startAt(absence.getStartAt())
            .endAt(absence.getEndAt())
            .reason(absence.getReason())
            .createdAt(absence.getCreatedAt())
            .updatedAt(absence.getUpdatedAt())
            .build();
    }

}


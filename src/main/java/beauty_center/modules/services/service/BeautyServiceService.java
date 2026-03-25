package beauty_center.modules.services.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.BeautyServiceEmployee;
import beauty_center.modules.services.repository.BeautyServiceEmployeeRepository;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import beauty_center.modules.services.repository.SpecialtyRepository;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Beauty service service managing available services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BeautyServiceService {

    private final BeautyServiceRepository beautyServiceRepository;
    private final BeautyServiceEmployeeRepository beautyServiceEmployeeRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    /**
     * Get all services
     */
    public List<BeautyService> getAllServices() {
        return beautyServiceRepository.findAll();
    }

    /**
     * Get all active services
     */
    public List<BeautyService> getAllActiveServices() {
        return beautyServiceRepository.findByIsActiveTrue();
    }

    /**
     * Get service by ID
     */
    public Optional<BeautyService> getServiceById(UUID id) {
        return beautyServiceRepository.findById(id);
    }

    /**
     * Create new service
     */
    public BeautyService createService(BeautyService service, List<UUID> allowedEmployeeIds) {
        // Validate price is positive
        if (service.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Validate duration is reasonable
        if (service.getDurationMin() < 15) {
            throw new IllegalArgumentException("Minimum duration is 15 minutes");
        }

        // Validate specialty exists if provided
        if (service.getSpecialtyId() != null && !specialtyRepository.existsById(service.getSpecialtyId())) {
            throw new EntityNotFoundException("Specialty", service.getSpecialtyId());
        }

        BeautyService created = beautyServiceRepository.save(service);

        try { auditService.logCreate("BeautyService", created.getId(), created); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        // Assign allowed employees if provided
        if (allowedEmployeeIds != null && !allowedEmployeeIds.isEmpty()) {
            assignEmployeesToService(created.getId(), allowedEmployeeIds);
        }

        return created;
    }

    /**
     * Update service
     */
    public BeautyService updateService(UUID id, BeautyService updates, List<UUID> allowedEmployeeIds) {
        BeautyService existing = beautyServiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("BeautyService", id));

        // Validate price is positive
        if (updates.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Validate duration is reasonable
        if (updates.getDurationMin() < 15) {
            throw new IllegalArgumentException("Minimum duration is 15 minutes");
        }

        // Validate specialty exists if provided
        if (updates.getSpecialtyId() != null && !specialtyRepository.existsById(updates.getSpecialtyId())) {
            throw new EntityNotFoundException("Specialty", updates.getSpecialtyId());
        }

        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setDurationMin(updates.getDurationMin());
        existing.setPrice(updates.getPrice());
        existing.setSpecialtyId(updates.getSpecialtyId());
        existing.setActive(updates.isActive());

        BeautyService updated = beautyServiceRepository.save(existing);

        try { auditService.logUpdate("BeautyService", id, null, updated); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        // Update allowed employees if provided
        if (allowedEmployeeIds != null) {
            // Remove all existing assignments
            beautyServiceEmployeeRepository.deleteByBeautyServiceId(id);
            // Add new assignments
            if (!allowedEmployeeIds.isEmpty()) {
                assignEmployeesToService(id, allowedEmployeeIds);
            }
        }

        return updated;
    }

    /**
     * Deactivate service (soft delete)
     */
    public void deactivateService(UUID id) {
        beautyServiceRepository.findById(id).ifPresent(service -> {
            service.setActive(false);
            beautyServiceRepository.save(service);
        });
    }

    /**
     * Activate service
     */
    public void activateService(UUID id) {
        beautyServiceRepository.findById(id).ifPresent(service -> {
            service.setActive(true);
            beautyServiceRepository.save(service);
        });
    }

    /**
     * Get allowed employees for a service
     */
    public List<UUID> getAllowedEmployees(UUID serviceId) {
        return beautyServiceEmployeeRepository.findByBeautyServiceId(serviceId)
            .stream()
            .map(BeautyServiceEmployee::getEmployeeId)
            .collect(Collectors.toList());
    }

    /**
     * Get services that an employee can perform
     * Filtered by employee allowed list and specialty match
     */
    public List<BeautyService> getServicesByEmployee(UUID employeeId) {
        // Validate employee exists
        if (!userAccountRepository.existsById(employeeId)) {
            throw new EntityNotFoundException("Employee", employeeId);
        }

        // Get all service IDs the employee is allowed to perform
        List<UUID> serviceIds = beautyServiceEmployeeRepository.findByEmployeeId(employeeId)
            .stream()
            .map(BeautyServiceEmployee::getBeautyServiceId)
            .collect(Collectors.toList());

        if (serviceIds.isEmpty()) {
            return List.of();
        }

        // Get the actual services (only active ones)
        return beautyServiceRepository.findAllById(serviceIds)
            .stream()
            .filter(BeautyService::isActive)
            .collect(Collectors.toList());
    }

    /**
     * Assign employees to service
     */
    private void assignEmployeesToService(UUID serviceId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            // Validate employee exists
            if (!userAccountRepository.existsById(employeeId)) {
                throw new EntityNotFoundException("Employee", employeeId);
            }

            // Skip if already assigned
            if (beautyServiceEmployeeRepository.existsByBeautyServiceIdAndEmployeeId(serviceId, employeeId)) {
                continue;
            }

            BeautyServiceEmployee assignment = BeautyServiceEmployee.builder()
                .beautyServiceId(serviceId)
                .employeeId(employeeId)
                .build();

            beautyServiceEmployeeRepository.save(assignment);
        }
    }

}

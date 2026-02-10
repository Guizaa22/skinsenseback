package beauty_center.modules.services.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.services.dto.AssignSpecialtyRequest;
import beauty_center.modules.services.dto.BeautyServiceResponse;
import beauty_center.modules.services.dto.SpecialtyResponse;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.Specialty;
import beauty_center.modules.services.service.BeautyServiceService;
import beauty_center.modules.services.service.EmployeeSpecialtyService;
import beauty_center.modules.services.service.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Employee Specialty REST controller (admin only for assignments)
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeSpecialtyController {

    private final EmployeeSpecialtyService employeeSpecialtyService;
    private final BeautyServiceService beautyServiceService;
    private final SpecialtyService specialtyService;

    /**
     * Get all specialties for an employee
     * Accessible by all authenticated users
     */
    @GetMapping("/{employeeId}/specialties")
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getEmployeeSpecialties(
            @PathVariable UUID employeeId) {
        log.info("Get specialties for employee: {}", employeeId);

        List<SpecialtyResponse> specialties = employeeSpecialtyService.getEmployeeSpecialties(employeeId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(specialties, "Employee specialties retrieved successfully"));
    }

    /**
     * Assign specialty to employee
     * Only ADMIN can assign specialties
     */
    @PostMapping("/{employeeId}/specialties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignSpecialtyToEmployee(
            @PathVariable UUID employeeId,
            @Valid @RequestBody AssignSpecialtyRequest request) {
        log.info("Assign specialty {} to employee {}", request.getSpecialtyId(), employeeId);

        try {
            employeeSpecialtyService.assignSpecialtyToEmployee(employeeId, request.getSpecialtyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Specialty assigned to employee successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Remove specialty from employee
     * Only ADMIN can remove specialties
     */
    @DeleteMapping("/{employeeId}/specialties/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeSpecialtyFromEmployee(
            @PathVariable UUID employeeId,
            @PathVariable UUID specialtyId) {
        log.info("Remove specialty {} from employee {}", specialtyId, employeeId);

        try {
            employeeSpecialtyService.removeSpecialtyFromEmployee(employeeId, specialtyId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Specialty removed from employee successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Get services that an employee can perform
     * Filtered by employee allowed list and specialty match
     * Accessible by all authenticated users
     */
    @GetMapping("/{employeeId}/services")
    public ResponseEntity<ApiResponse<List<BeautyServiceResponse>>> getEmployeeServices(
            @PathVariable UUID employeeId) {
        log.info("Get services for employee: {}", employeeId);

        try {
            List<BeautyService> services = beautyServiceService.getServicesByEmployee(employeeId);

            List<BeautyServiceResponse> responses = services.stream()
                .map(this::toServiceResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.ok(responses, "Employee services retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Convert Specialty entity to SpecialtyResponse DTO
     */
    private SpecialtyResponse toResponse(Specialty specialty) {
        return SpecialtyResponse.builder()
            .id(specialty.getId())
            .name(specialty.getName())
            .description(specialty.getDescription())
            .createdAt(specialty.getCreatedAt())
            .updatedAt(specialty.getUpdatedAt())
            .build();
    }

    /**
     * Convert BeautyService entity to BeautyServiceResponse DTO
     */
    private BeautyServiceResponse toServiceResponse(BeautyService service) {
        String specialtyName = null;
        if (service.getSpecialtyId() != null) {
            specialtyName = specialtyService.getSpecialtyById(service.getSpecialtyId())
                .map(Specialty::getName)
                .orElse(null);
        }

        List<UUID> allowedEmployeeIds = beautyServiceService.getAllowedEmployees(service.getId());

        return BeautyServiceResponse.builder()
            .id(service.getId())
            .name(service.getName())
            .description(service.getDescription())
            .durationMinutes(service.getDurationMin())
            .price(service.getPrice())
            .isActive(service.isActive())
            .specialtyId(service.getSpecialtyId())
            .specialtyName(specialtyName)
            .allowedEmployeeIds(allowedEmployeeIds)
            .createdAt(service.getCreatedAt())
            .updatedAt(service.getUpdatedAt())
            .build();
    }

}


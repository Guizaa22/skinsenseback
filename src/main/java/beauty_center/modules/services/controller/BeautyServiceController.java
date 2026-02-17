package beauty_center.modules.services.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.services.dto.BeautyServiceCreateRequest;
import beauty_center.modules.services.dto.BeautyServiceResponse;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.Specialty;
import beauty_center.modules.services.service.BeautyServiceService;
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
 * Beauty service REST controller
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class BeautyServiceController {

    private final BeautyServiceService beautyServiceService;
    private final SpecialtyService specialtyService;

    /**
     * Get services with optional active filter
     * Accessible by all authenticated users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BeautyServiceResponse>>> getServices(
            @RequestParam(required = false) Boolean active) {
        log.info("Get services request, active filter: {}", active);

        List<BeautyService> services;
        if (active != null && active) {
            services = beautyServiceService.getAllActiveServices();
        } else {
            services = beautyServiceService.getAllServices();
        }

        List<BeautyServiceResponse> responses = services.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(responses, "Services retrieved successfully"));
    }

    /**
     * Get service by ID
     * Accessible by all authenticated users
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> getServiceById(@PathVariable UUID id) {
        log.info("Get service by ID: {}", id);

        BeautyService service = beautyServiceService.getServiceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        return ResponseEntity.ok(ApiResponse.ok(toResponse(service), "Service retrieved successfully"));
    }

    /**
     * Create new service
     * Only ADMIN can create services
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> createService(
            @Valid @RequestBody BeautyServiceCreateRequest request) {
        log.info("Create service request: {}", request.getName());

        try {
            BeautyService service = BeautyService.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .durationMin(request.getDurationMinutes())
                    .price(request.getPrice())
                    .specialtyId(request.getSpecialtyId())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .build();

            BeautyService created = beautyServiceService.createService(service, request.getAllowedEmployeeIds());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(toResponse(created), "Service created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Update service
     * Only ADMIN can update services
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody BeautyServiceCreateRequest request) {
        log.info("Update service request for ID: {}", id);

        try {
            BeautyService updates = BeautyService.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .durationMin(request.getDurationMinutes())
                    .price(request.getPrice())
                    .specialtyId(request.getSpecialtyId())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .build();

            BeautyService updated = beautyServiceService.updateService(id, updates, request.getAllowedEmployeeIds());

            return ResponseEntity.ok(ApiResponse.ok(toResponse(updated), "Service updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Deactivate service (soft delete)
     * Only ADMIN can deactivate services
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable UUID id) {
        log.info("Deactivate service request for ID: {}", id);

        try {
            beautyServiceService.deactivateService(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Service deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Convert BeautyService entity to BeautyServiceResponse DTO
     */
    private BeautyServiceResponse toResponse(BeautyService service) {
        String specialtyName = null;
        if (service.getSpecialtyId() != null) {
            specialtyName = specialtyService.getSpecialtyById(service.getSpecialtyId())
                    .map(Specialty::getName)
                    .orElse(null);
        }

        List<UUID> allowedEmployeeIds = null;
        // Only include employee IDs for ADMIN users
        try {
            // Check if current user has ADMIN role
            if (beauty_center.security.SecurityUtils.hasRole("ADMIN")) {
                allowedEmployeeIds = beautyServiceService.getAllowedEmployees(service.getId());
            }
        } catch (Exception e) {
            // If checking role fails (e.g. anonymous user), keep allowedEmployeeIds as null
            log.debug("Failed to check admin role, hiding employee IDs: {}", e.getMessage());
        }

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

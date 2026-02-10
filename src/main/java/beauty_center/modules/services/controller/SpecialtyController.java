package beauty_center.modules.services.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.services.dto.SpecialtyCreateRequest;
import beauty_center.modules.services.dto.SpecialtyResponse;
import beauty_center.modules.services.entity.Specialty;
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
 * Specialty REST controller (admin only)
 */
@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Slf4j
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    /**
     * Get all specialties
     * Accessible by all authenticated users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getAllSpecialties() {
        log.info("Get all specialties request");
        
        List<SpecialtyResponse> specialties = specialtyService.getAllSpecialties()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(specialties, "Specialties retrieved successfully"));
    }

    /**
     * Get specialty by ID
     * Accessible by all authenticated users
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> getSpecialtyById(@PathVariable UUID id) {
        log.info("Get specialty by ID: {}", id);

        Specialty specialty = specialtyService.getSpecialtyById(id)
            .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));

        return ResponseEntity.ok(ApiResponse.ok(toResponse(specialty), "Specialty retrieved successfully"));
    }

    /**
     * Create new specialty
     * Only ADMIN can create specialties
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> createSpecialty(
            @Valid @RequestBody SpecialtyCreateRequest request) {
        log.info("Create specialty request: {}", request.getName());

        try {
            Specialty specialty = Specialty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

            Specialty created = specialtyService.createSpecialty(specialty);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(toResponse(created), "Specialty created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Update specialty
     * Only ADMIN can update specialties
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> updateSpecialty(
            @PathVariable UUID id,
            @Valid @RequestBody SpecialtyCreateRequest request) {
        log.info("Update specialty request for ID: {}", id);

        try {
            Specialty updates = Specialty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

            Specialty updated = specialtyService.updateSpecialty(id, updates);

            return ResponseEntity.ok(ApiResponse.ok(toResponse(updated), "Specialty updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Delete specialty
     * Only ADMIN can delete specialties
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSpecialty(@PathVariable UUID id) {
        log.info("Delete specialty request for ID: {}", id);

        try {
            specialtyService.deleteSpecialty(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Specialty deleted successfully"));
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

}


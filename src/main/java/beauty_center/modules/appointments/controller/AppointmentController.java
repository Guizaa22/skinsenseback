package beauty_center.modules.appointments.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.appointments.dto.AppointmentCancelRequest;
import beauty_center.modules.appointments.dto.AppointmentCreateRequest;
import beauty_center.modules.appointments.dto.AppointmentResponse;
import beauty_center.modules.appointments.dto.AppointmentUpdateRequest;
import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.service.AppointmentService;
import beauty_center.modules.appointments.service.BookingService;
import beauty_center.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Appointment REST controller for booking management.
 * Implements role-based access control:
 * - CLIENT: Can view own appointments, create appointments, cancel own appointments
 * - EMPLOYEE: Can view own calendar, update/complete appointments
 * - ADMIN: Full access to all appointments
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final BookingService bookingService;
    private final CurrentUser currentUser;

    /**
     * Get appointments with optional filters.
     * - CLIENT: sees only own appointments
     * - EMPLOYEE: sees own calendar
     * - ADMIN: sees all (can filter by employee/client)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointments(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        log.info("Get appointments request by: {}", currentUser.getUsername());

        UUID currentUserId = currentUser.getUserId();

        // Role-based filtering
        if (currentUser.hasRole("CLIENT")) {
            // Clients can only see their own appointments
            clientId = currentUserId;
            employeeId = null; // Ignore employee filter for clients
        } else if (currentUser.hasRole("EMPLOYEE") && !currentUser.hasRole("ADMIN")) {
            // Employees see their own calendar (unless they're also admin)
            employeeId = currentUserId;
            if (clientId != null) {
                // Employees can filter by client on their calendar
                log.debug("Employee {} filtering by client {}", currentUserId, clientId);
            }
        }
        // ADMIN can use filters as provided

        List<Appointment> appointments = appointmentService.getAppointments(
                clientId, employeeId, status, from, to);

        List<AppointmentResponse> response = appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(response, "Appointments retrieved successfully"));
    }

    /**
     * Get appointment by ID.
     * Access control: Own appointment (client/employee) or ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable UUID id) {
        log.info("Get appointment {} requested by: {}", id, currentUser.getUsername());

        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        // Check access: must be client, employee, or admin
        UUID currentUserId = currentUser.getUserId();
        boolean isOwner = appointment.getClientId().equals(currentUserId) ||
                         appointment.getEmployeeId().equals(currentUserId);

        if (!isOwner && !currentUser.hasRole("ADMIN")) {
            log.warn("User {} attempted to access appointment {} without permission", currentUserId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", HttpStatus.FORBIDDEN.value()));
        }

        return ResponseEntity.ok(ApiResponse.ok(toResponse(appointment), "Appointment retrieved successfully"));
    }

    /**
     * Create new appointment.
     * - CLIENT: books for themselves
     * - ADMIN: can book for any client (specify clientId)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {

        log.info("Create appointment request by: {}", currentUser.getUsername());

        UUID clientId = request.getClientId();

        // If clientId not specified, use current user
        if (clientId == null) {
            clientId = currentUser.getUserId();
        } else {
            // Only ADMIN can book for another client
            if (!currentUser.hasRole("ADMIN")) {
                log.warn("Non-admin user {} attempted to book for client {}",
                        currentUser.getUserId(), clientId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Only admins can book for other clients",
                                HttpStatus.FORBIDDEN.value()));
            }
        }

        try {
            Appointment appointment = bookingService.createAppointment(
                    clientId,
                    request.getEmployeeId(),
                    request.getServiceId(),
                    request.getStartAt(),
                    request.getNotes());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(toResponse(appointment), "Appointment created successfully"));

        } catch (BookingService.AppointmentConflictException e) {
            log.warn("Appointment conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
        }
    }


    /**
     * Update/reschedule appointment.
     * Only ADMIN and EMPLOYEE can reschedule appointments.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentUpdateRequest request) {

        log.info("Update appointment {} requested by: {}", id, currentUser.getUsername());

        try {
            Appointment appointment = appointmentService.updateAppointment(
                    id,
                    request.getEmployeeId(),
                    request.getServiceId(),
                    request.getStartAt());

            return ResponseEntity.ok(
                    ApiResponse.ok(toResponse(appointment), "Appointment rescheduled successfully"));

        } catch (BookingService.AppointmentConflictException e) {
            log.warn("Appointment conflict during update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
        } catch (IllegalStateException e) {
            log.warn("Invalid appointment state for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Cancel appointment.
     * - CLIENT: can cancel own appointments
     * - EMPLOYEE/ADMIN: can cancel any appointment
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentCancelRequest request) {

        log.info("Cancel appointment {} requested by: {}", id, currentUser.getUsername());

        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        // Check access: client can cancel own, employee/admin can cancel any
        UUID currentUserId = currentUser.getUserId();
        boolean isClient = appointment.getClientId().equals(currentUserId);
        boolean canCancel = isClient || currentUser.hasAnyRole("EMPLOYEE", "ADMIN");

        if (!canCancel) {
            log.warn("User {} attempted to cancel appointment {} without permission", currentUserId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied", HttpStatus.FORBIDDEN.value()));
        }

        try {
            appointmentService.cancelAppointment(id, request.getCancellationReason());
            return ResponseEntity.ok(ApiResponse.ok(null, "Appointment canceled successfully"));

        } catch (IllegalStateException e) {
            log.warn("Cannot cancel appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Mark appointment as completed.
     * Only EMPLOYEE and ADMIN can complete appointments.
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> completeAppointment(@PathVariable UUID id) {
        log.info("Complete appointment {} requested by: {}", id, currentUser.getUsername());

        try {
            appointmentService.completeAppointment(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Appointment marked as completed"));

        } catch (IllegalStateException e) {
            log.warn("Cannot complete appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Convert Appointment entity to AppointmentResponse DTO
     */
    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .clientId(appointment.getClientId())
                .employeeId(appointment.getEmployeeId())
                .serviceId(appointment.getBeautyServiceId())
                .startAt(appointment.getStartAt())
                .endAt(appointment.getEndAt())
                .status(appointment.getStatus().name())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

}

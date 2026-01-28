package beauty_center.modules.appointments.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.appointments.dto.AppointmentCreateRequest;
import beauty_center.modules.appointments.dto.AppointmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Appointment REST controller for booking management
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    // TODO: Inject AppointmentService

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAppointments() {
        // TODO: Get appointments with filters (client/employee/status)
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable UUID id) {
        // TODO: Get appointment by ID
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        // TODO: Create appointment with availability validation
        return ResponseEntity.ok(ApiResponse.ok(null, "Appointment created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentCreateRequest request) {
        // TODO: Update appointment
        return ResponseEntity.ok(ApiResponse.ok(null, "Appointment updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        // TODO: Cancel appointment with reason tracking
        return ResponseEntity.ok(ApiResponse.ok(null, "Appointment canceled"));
    }

}

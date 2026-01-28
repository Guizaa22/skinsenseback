package beauty_center.modules.scheduling.controller;

import beauty_center.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Scheduling controller for managing work schedules and availability
 */
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    // TODO: Inject AvailabilityService

    @GetMapping("/availability/{employeeId}")
    public ResponseEntity<ApiResponse<?>> checkAvailability(
            @PathVariable UUID employeeId,
            @RequestParam LocalDateTime startAt,
            @RequestParam LocalDateTime endAt) {
        // TODO: Check employee availability
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/next-slot/{employeeId}")
    public ResponseEntity<ApiResponse<?>> getNextAvailableSlot(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(defaultValue = "60") int durationMinutes) {
        // TODO: Find next available slot
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}

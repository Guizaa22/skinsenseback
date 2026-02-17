package beauty_center.modules.scheduling.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.scheduling.dto.AvailabilityResponse;
import beauty_center.modules.scheduling.dto.TimeSlot;
import beauty_center.modules.scheduling.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Scheduling controller for managing work schedules and availability.
 * Provides endpoints for checking employee availability for booking appointments.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SchedulingController {

    private final AvailabilityService availabilityService;

    /**
     * Get available time slots for an employee and service.
     * Query parameters:
     * - employeeId: UUID of the employee
     * - serviceId: UUID of the service
     * - date: Start date in ISO format (YYYY-MM-DD)
     * - days: Optional, number of days to check (default: 1, max: 30)
     *
     * Example: GET /api/availability?employeeId=...&serviceId=...&date=2024-02-15&days=7
     */
    @GetMapping("/availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(
            @RequestParam UUID employeeId,
            @RequestParam UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int days) {

        log.info("Availability request for employee {} and service {} from {} for {} days",
                employeeId, serviceId, date, days);

        // Validate days parameter
        if (days < 1 || days > 30) {
            throw new IllegalArgumentException("Days parameter must be between 1 and 30");
        }

        List<TimeSlot> availableSlots = availabilityService.getAvailableSlots(
                employeeId, serviceId, date, days);

        LocalDate endDate = date.plusDays(days - 1);

        AvailabilityResponse response = AvailabilityResponse.builder()
                .employeeId(employeeId)
                .serviceId(serviceId)
                .startDate(date)
                .endDate(endDate)
                .availableSlots(availableSlots)
                .build();

        return ResponseEntity.ok(
                ApiResponse.ok(response, "Availability retrieved successfully"));
    }

}

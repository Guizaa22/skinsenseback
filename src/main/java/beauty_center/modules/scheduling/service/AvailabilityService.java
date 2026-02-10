package beauty_center.modules.scheduling.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.scheduling.dto.TimeSlot;
import beauty_center.modules.scheduling.entity.Absence;
import beauty_center.modules.scheduling.entity.WorkingTimeSlot;
import beauty_center.modules.scheduling.repository.AbsenceRepository;
import beauty_center.modules.scheduling.repository.WorkingTimeSlotRepository;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Availability service calculating employee availability.
 * Core engine for appointment scheduling validation.
 *
 * Algorithm:
 * 1. Get employee's working hours for requested days
 * 2. Get absences in date range
 * 3. Get existing non-canceled appointments
 * 4. Generate time slots with 15-minute granularity
 * 5. Filter out slots that overlap with absences or appointments
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AvailabilityService {

    private static final int SLOT_GRANULARITY_MINUTES = 15;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Africa/Tunis");

    private final WorkingTimeSlotRepository workingTimeSlotRepository;
    private final AbsenceRepository absenceRepository;
    private final AppointmentRepository appointmentRepository;
    private final BeautyServiceRepository beautyServiceRepository;

    /**
     * Get available time slots for an employee and service.
     * Convenience method that retrieves service duration and calculates availability.
     *
     * @param employeeId Employee UUID
     * @param serviceId Service UUID
     * @param startDate Start date (inclusive)
     * @param days Number of days to check (1-30)
     * @return List of available time slots
     * @throws IllegalArgumentException if service not found or inactive
     */
    public List<TimeSlot> getAvailableSlots(
            UUID employeeId,
            UUID serviceId,
            LocalDate startDate,
            int days) {

        // Validate and retrieve service
        BeautyService service = beautyServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

        if (!service.isActive()) {
            throw new IllegalArgumentException("Service is not active: " + serviceId);
        }

        // Calculate end date
        LocalDate endDate = startDate.plusDays(days - 1);

        // Delegate to existing method
        return calculateAvailableSlots(employeeId, service.getDurationMin(), startDate, endDate);
    }

    /**
     * Calculate available time slots for an employee within a date range.
     *
     * @param employeeId Employee UUID
     * @param serviceDurationMinutes Duration of service in minutes
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of available time slots
     */
    public List<TimeSlot> calculateAvailableSlots(
            UUID employeeId,
            int serviceDurationMinutes,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Calculating availability for employee {} from {} to {} (duration: {} min)",
                employeeId, startDate, endDate, serviceDurationMinutes);

        List<TimeSlot> availableSlots = new ArrayList<>();

        // Process each day in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            List<TimeSlot> dailySlots = calculateDailyAvailableSlots(
                    employeeId, serviceDurationMinutes, currentDate);
            availableSlots.addAll(dailySlots);
            currentDate = currentDate.plusDays(1);
        }

        log.debug("Found {} available slots for employee {}", availableSlots.size(), employeeId);
        return availableSlots;
    }

    /**
     * Calculate available slots for a single day.
     */
    private List<TimeSlot> calculateDailyAvailableSlots(
            UUID employeeId,
            int serviceDurationMinutes,
            LocalDate date) {

        // Get day of week (MON, TUE, etc.)
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        // Get working hours for this day
        List<WorkingTimeSlot> workingSlots = workingTimeSlotRepository
                .findByEmployeeIdAndDayOfWeek(employeeId, dayOfWeek);

        if (workingSlots.isEmpty()) {
            log.debug("No working hours defined for employee {} on {}", employeeId, dayOfWeek);
            return Collections.emptyList();
        }

        // Get absences for this day
        OffsetDateTime dayStart = date.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
        List<Absence> absences = absenceRepository.findByEmployeeIdAndDateRange(
                employeeId, dayStart, dayEnd);

        // Get existing appointments for this day (non-canceled)
        List<Appointment> appointments = appointmentRepository
                .findNonCanceledByEmployeeIdAndDateRange(employeeId, dayStart, dayEnd);

        // Generate candidate slots from working hours
        List<TimeSlot> candidateSlots = new ArrayList<>();
        for (WorkingTimeSlot workingSlot : workingSlots) {
            candidateSlots.addAll(generateSlotsFromWorkingHours(
                    date, workingSlot, serviceDurationMinutes));
        }

        // Filter out slots that overlap with absences or appointments
        return candidateSlots.stream()
                .filter(slot -> !overlapsWithAbsences(slot, absences))
                .filter(slot -> !overlapsWithAppointments(slot, appointments))
                .collect(Collectors.toList());
    }

    /**
     * Generate time slots from working hours with specified granularity.
     */
    private List<TimeSlot> generateSlotsFromWorkingHours(
            LocalDate date,
            WorkingTimeSlot workingSlot,
            int serviceDurationMinutes) {

        List<TimeSlot> slots = new ArrayList<>();

        LocalTime currentTime = workingSlot.getStartTime();
        LocalTime endTime = workingSlot.getEndTime();

        while (currentTime.plusMinutes(serviceDurationMinutes).compareTo(endTime) <= 0) {
            OffsetDateTime slotStart = OffsetDateTime.of(date, currentTime, ZoneOffset.ofHours(1));
            OffsetDateTime slotEnd = slotStart.plusMinutes(serviceDurationMinutes);

            slots.add(TimeSlot.builder()
                    .startAt(slotStart)
                    .endAt(slotEnd)
                    .build());

            currentTime = currentTime.plusMinutes(SLOT_GRANULARITY_MINUTES);
        }

        return slots;
    }


    /**
     * Check if a time slot overlaps with any absences.
     */
    private boolean overlapsWithAbsences(TimeSlot slot, List<Absence> absences) {
        return absences.stream()
                .anyMatch(absence -> timesOverlap(
                        slot.getStartAt(), slot.getEndAt(),
                        absence.getStartAt(), absence.getEndAt()));
    }

    /**
     * Check if a time slot overlaps with any appointments.
     */
    private boolean overlapsWithAppointments(TimeSlot slot, List<Appointment> appointments) {
        return appointments.stream()
                .anyMatch(appointment -> timesOverlap(
                        slot.getStartAt(), slot.getEndAt(),
                        appointment.getStartAt(), appointment.getEndAt()));
    }

    /**
     * Check if two time ranges overlap.
     * Overlap occurs when: start1 < end2 AND end1 > start2
     */
    private boolean timesOverlap(
            OffsetDateTime start1, OffsetDateTime end1,
            OffsetDateTime start2, OffsetDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Check if employee is available at a specific time slot.
     * Used for validation before booking.
     *
     * @param employeeId Employee UUID
     * @param startAt Slot start time
     * @param endAt Slot end time
     * @return true if available, false otherwise
     */
    public boolean isAvailable(UUID employeeId, OffsetDateTime startAt, OffsetDateTime endAt) {
        log.debug("Checking availability for employee {} from {} to {}", employeeId, startAt, endAt);

        // Check working hours
        LocalDate date = startAt.toLocalDate();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        List<WorkingTimeSlot> workingSlots = workingTimeSlotRepository
                .findByEmployeeIdAndDayOfWeek(employeeId, dayOfWeek);

        if (workingSlots.isEmpty()) {
            log.debug("Employee {} does not work on {}", employeeId, dayOfWeek);
            return false;
        }

        // Check if requested time falls within working hours
        LocalTime requestedStart = startAt.toLocalTime();
        LocalTime requestedEnd = endAt.toLocalTime();

        boolean withinWorkingHours = workingSlots.stream()
                .anyMatch(slot ->
                    !requestedStart.isBefore(slot.getStartTime()) &&
                    !requestedEnd.isAfter(slot.getEndTime()));

        if (!withinWorkingHours) {
            log.debug("Requested time is outside working hours for employee {}", employeeId);
            return false;
        }

        // Check for absences
        List<Absence> absences = absenceRepository.findByEmployeeIdAndDateRange(
                employeeId, startAt, endAt);

        if (!absences.isEmpty()) {
            log.debug("Employee {} has absence during requested time", employeeId);
            return false;
        }

        // Check for overlapping appointments (excluding canceled)
        List<Appointment> appointments = appointmentRepository
                .findNonCanceledByEmployeeIdAndDateRange(employeeId, startAt, endAt);

        if (!appointments.isEmpty()) {
            log.debug("Employee {} has existing appointment during requested time", employeeId);
            return false;
        }

        return true;
    }

}

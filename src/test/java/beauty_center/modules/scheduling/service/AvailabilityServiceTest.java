package beauty_center.modules.scheduling.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.scheduling.dto.TimeSlot;
import beauty_center.modules.scheduling.entity.Absence;
import beauty_center.modules.scheduling.entity.WorkingTimeSlot;
import beauty_center.modules.scheduling.repository.AbsenceRepository;
import beauty_center.modules.scheduling.repository.WorkingTimeSlotRepository;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AvailabilityService.
 * Tests edge cases: boundary times, exact fit, overlaps, absence scenarios, timezone handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService Unit Tests")
class AvailabilityServiceTest {

    @Mock
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Mock
    private AbsenceRepository absenceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private BeautyServiceRepository beautyServiceRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private UUID employeeId;
    private UUID serviceId;
    private BeautyService testService;
    private LocalDate testDate;
    private ZoneOffset tunisiaOffset;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        testDate = LocalDate.of(2024, 2, 15); // Thursday
        tunisiaOffset = ZoneOffset.ofHours(1); // Tunisia is UTC+1

        // Create test service with 60-minute duration
        testService = BeautyService.builder()
                .id(serviceId)
                .name("Test Service")
                .durationMin(60)
                .price(BigDecimal.valueOf(50.00))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should return empty list when employee has no working hours")
    void testNoWorkingHours() {
        // Given: No working hours for the employee
        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.of(testService));
        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(eq(employeeId), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then
        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when service not found")
    void testServiceNotFound() {
        // Given: Service doesn't exist
        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1));
    }

    @Test
    @DisplayName("Should throw exception when service is inactive")
    void testInactiveService() {
        // Given: Service is inactive
        testService.setActive(false);
        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.of(testService));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1));
    }

    @Test
    @DisplayName("Should generate slots with 15-minute granularity")
    void testFifteenMinuteGranularity() {
        // Given: Working hours 9:00-12:00 (3 hours = 180 minutes)
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(12, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Should have slots at 9:00, 9:15, 9:30, 9:45, 10:00, 10:15, 10:30, 10:45, 11:00
        // (60-min service needs to end by 12:00, so last slot starts at 11:00)
        assertThat(slots).hasSize(9);

        // Verify first slot starts at 9:00
        assertThat(slots.get(0).getStartAt().toLocalTime()).isEqualTo(LocalTime.of(9, 0));

        // Verify last slot starts at 11:00
        assertThat(slots.get(8).getStartAt().toLocalTime()).isEqualTo(LocalTime.of(11, 0));

        // Verify 15-minute intervals
        for (int i = 0; i < slots.size() - 1; i++) {
            Duration gap = Duration.between(slots.get(i).getStartAt(), slots.get(i + 1).getStartAt());
            assertThat(gap.toMinutes()).isEqualTo(15);
        }
    }

    @Test
    @DisplayName("Should handle boundary times - slot at exact start of working hours")
    void testBoundaryTimeAtStart() {
        // Given: Working hours start at 9:00
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: First slot should start exactly at 9:00
        assertThat(slots).isNotEmpty();
        assertThat(slots.get(0).getStartAt().toLocalTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("Should handle boundary times - last possible slot at end of working hours")
    void testBoundaryTimeAtEnd() {
        // Given: Working hours 9:00-17:00, 60-min service
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Last slot should start at 16:00 (ends at 17:00)
        assertThat(slots).isNotEmpty();
        TimeSlot lastSlot = slots.get(slots.size() - 1);
        assertThat(lastSlot.getStartAt().toLocalTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(lastSlot.getEndAt().toLocalTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    @DisplayName("Should handle exact fit - service duration exactly fills available slot")
    void testExactFit() {
        // Given: Working hours 9:00-10:00 (exactly 60 minutes), 60-min service
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Should have exactly one slot at 9:00
        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).getStartAt().toLocalTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(0).getEndAt().toLocalTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("Should exclude slots overlapping with existing appointments")
    void testOverlapsWithAppointments() {
        // Given: Working hours 9:00-17:00
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Existing appointment from 10:00-11:00
        Appointment existingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .clientId(UUID.randomUUID())
                .beautyServiceId(serviceId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(10, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(11, 0), tunisiaOffset))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(existingAppointment));

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: No slots should overlap with 10:00-11:00
        for (TimeSlot slot : slots) {
            LocalTime slotStart = slot.getStartAt().toLocalTime();
            LocalTime slotEnd = slot.getEndAt().toLocalTime();

            // Slot should not overlap with appointment (10:00-11:00)
            boolean overlaps = slotStart.isBefore(LocalTime.of(11, 0)) &&
                             slotEnd.isAfter(LocalTime.of(10, 0));

            assertThat(overlaps).isFalse();
        }
    }

    @Test
    @DisplayName("Should exclude slots overlapping with absences")
    void testOverlapsWithAbsences() {
        // Given: Working hours 9:00-17:00
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Absence from 14:00-15:00
        Absence absence = Absence.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(14, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(15, 0), tunisiaOffset))
                .reason("Lunch break")
                .build();

        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(absence));

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: No slots should overlap with 14:00-15:00
        for (TimeSlot slot : slots) {
            LocalTime slotStart = slot.getStartAt().toLocalTime();
            LocalTime slotEnd = slot.getEndAt().toLocalTime();

            // Slot should not overlap with absence (14:00-15:00)
            boolean overlaps = slotStart.isBefore(LocalTime.of(15, 0)) &&
                             slotEnd.isAfter(LocalTime.of(14, 0));

            assertThat(overlaps).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle absence cutting day in half")
    void testAbsenceCuttingDayInHalf() {
        // Given: Working hours 9:00-17:00, absence 12:00-14:00 (lunch)
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        Absence lunchBreak = Absence.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(12, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(14, 0), tunisiaOffset))
                .reason("Lunch")
                .build();

        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(lunchBreak));

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Should have slots before 12:00 and after 14:00
        assertThat(slots).isNotEmpty();

        // All slots should either end by 12:00 or start at/after 14:00
        for (TimeSlot slot : slots) {
            LocalTime slotStart = slot.getStartAt().toLocalTime();
            LocalTime slotEnd = slot.getEndAt().toLocalTime();

            boolean beforeLunch = slotEnd.isBefore(LocalTime.of(12, 0)) ||
                                slotEnd.equals(LocalTime.of(12, 0));
            boolean afterLunch = slotStart.isAfter(LocalTime.of(14, 0)) ||
                               slotStart.equals(LocalTime.of(14, 0));

            assertThat(beforeLunch || afterLunch).isTrue();
        }
    }

    @Test
    @DisplayName("Should handle multiple absences in one day")
    void testMultipleAbsences() {
        // Given: Working hours 9:00-17:00, two absences
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        Absence absence1 = Absence.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(10, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(11, 0), tunisiaOffset))
                .reason("Meeting")
                .build();

        Absence absence2 = Absence.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(14, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(15, 0), tunisiaOffset))
                .reason("Break")
                .build();

        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(absence1, absence2));

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: No slots should overlap with either absence
        for (TimeSlot slot : slots) {
            LocalTime slotStart = slot.getStartAt().toLocalTime();
            LocalTime slotEnd = slot.getEndAt().toLocalTime();

            boolean overlapsAbsence1 = slotStart.isBefore(LocalTime.of(11, 0)) &&
                                      slotEnd.isAfter(LocalTime.of(10, 0));
            boolean overlapsAbsence2 = slotStart.isBefore(LocalTime.of(15, 0)) &&
                                      slotEnd.isAfter(LocalTime.of(14, 0));

            assertThat(overlapsAbsence1 || overlapsAbsence2).isFalse();
        }
    }

    @Test
    @DisplayName("Should ignore canceled appointments when calculating availability")
    void testCanceledAppointmentsDontBlock() {
        // Given: Working hours 9:00-17:00
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Canceled appointment from 10:00-11:00 (should NOT block slots)
        Appointment canceledAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .clientId(UUID.randomUUID())
                .beautyServiceId(serviceId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(10, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(11, 0), tunisiaOffset))
                .status(AppointmentStatus.CANCELED)
                .build();

        // Repository should only return non-canceled appointments
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Should have slots in the 10:00-11:00 range (canceled appointment doesn't block)
        boolean hasSlotInRange = slots.stream()
                .anyMatch(slot -> {
                    LocalTime start = slot.getStartAt().toLocalTime();
                    return start.equals(LocalTime.of(10, 0)) || start.equals(LocalTime.of(10, 15)) ||
                           start.equals(LocalTime.of(10, 30)) || start.equals(LocalTime.of(10, 45));
                });

        assertThat(hasSlotInRange).isTrue();
    }

    @Test
    @DisplayName("Should calculate availability for multiple days")
    void testMultiDayAvailability() {
        // Given: Working hours 9:00-12:00 for multiple days
        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.of(testService));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Setup working hours for Thursday (testDate)
        WorkingTimeSlot thursdaySlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek("THU")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // Setup working hours for Friday (next day)
        WorkingTimeSlot fridaySlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek("FRI")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(employeeId, "THU"))
                .thenReturn(List.of(thursdaySlot));
        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(employeeId, "FRI"))
                .thenReturn(List.of(fridaySlot));

        // When: Request 2 days of availability
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 2);

        // Then: Should have slots from both days
        assertThat(slots).isNotEmpty();

        // Should have slots for Thursday
        boolean hasThursdaySlots = slots.stream()
                .anyMatch(slot -> slot.getStartAt().toLocalDate().equals(testDate));

        // Should have slots for Friday
        boolean hasFridaySlots = slots.stream()
                .anyMatch(slot -> slot.getStartAt().toLocalDate().equals(testDate.plusDays(1)));

        assertThat(hasThursdaySlots).isTrue();
        assertThat(hasFridaySlots).isTrue();
    }

    @Test
    @DisplayName("Should handle timezone correctly with OffsetDateTime")
    void testTimezoneHandling() {
        // Given: Working hours 9:00-12:00 with Tunisia timezone (UTC+1)
        setupWorkingHours(LocalTime.of(9, 0), LocalTime.of(12, 0));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: All slots should have Tunisia offset (UTC+1)
        assertThat(slots).isNotEmpty();
        for (TimeSlot slot : slots) {
            assertThat(slot.getStartAt().getOffset()).isEqualTo(tunisiaOffset);
            assertThat(slot.getEndAt().getOffset()).isEqualTo(tunisiaOffset);
        }
    }

    @Test
    @DisplayName("Should return empty list when service duration exceeds working hours")
    void testServiceLongerThanWorkingHours() {
        // Given: Working hours 9:00-10:00 (60 minutes), but service is 120 minutes
        BeautyService longService = BeautyService.builder()
                .id(serviceId)
                .name("Long Service")
                .durationMin(120)
                .price(BigDecimal.valueOf(100.00))
                .isActive(true)
                .build();

        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.of(longService));

        String dayOfWeek = testDate.getDayOfWeek().name().substring(0, 3).toUpperCase();

        WorkingTimeSlot shortSlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(eq(employeeId), any()))
                .thenReturn(List.of(shortSlot));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        // When
        List<TimeSlot> slots = availabilityService.getAvailableSlots(employeeId, serviceId, testDate, 1);

        // Then: Should return empty list (service doesn't fit)
        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Should validate single slot availability with isAvailable method")
    void testIsAvailableMethod() {
        // Given: Working hours 9:00-17:00
        String dayOfWeek = testDate.getDayOfWeek().name().substring(0, 3).toUpperCase();

        WorkingTimeSlot workingSlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(eq(employeeId), any()))
                .thenReturn(List.of(workingSlot));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        OffsetDateTime slotStart = OffsetDateTime.of(testDate, LocalTime.of(10, 0), tunisiaOffset);
        OffsetDateTime slotEnd = OffsetDateTime.of(testDate, LocalTime.of(11, 0), tunisiaOffset);

        // When
        boolean available = availabilityService.isAvailable(employeeId, slotStart, slotEnd);

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Should return false for isAvailable when slot overlaps with appointment")
    void testIsAvailableWithConflict() {
        // Given: Working hours 9:00-17:00 with existing appointment
        String dayOfWeek = testDate.getDayOfWeek().name().substring(0, 3).toUpperCase();

        WorkingTimeSlot workingSlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(eq(employeeId), any()))
                .thenReturn(List.of(workingSlot));
        when(absenceRepository.findByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        Appointment existingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .clientId(UUID.randomUUID())
                .beautyServiceId(serviceId)
                .startAt(OffsetDateTime.of(testDate, LocalTime.of(10, 0), tunisiaOffset))
                .endAt(OffsetDateTime.of(testDate, LocalTime.of(11, 0), tunisiaOffset))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findNonCanceledByEmployeeIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(existingAppointment));

        OffsetDateTime slotStart = OffsetDateTime.of(testDate, LocalTime.of(10, 30), tunisiaOffset);
        OffsetDateTime slotEnd = OffsetDateTime.of(testDate, LocalTime.of(11, 30), tunisiaOffset);

        // When
        boolean available = availabilityService.isAvailable(employeeId, slotStart, slotEnd);

        // Then: Should be false (overlaps with 10:00-11:00 appointment)
        assertThat(available).isFalse();
    }

    // ===== Helper Methods =====

    /**
     * Helper method to setup working hours for the test employee
     */
    private void setupWorkingHours(LocalTime startTime, LocalTime endTime) {
        when(beautyServiceRepository.findById(serviceId)).thenReturn(Optional.of(testService));

        String dayOfWeek = testDate.getDayOfWeek().name().substring(0, 3).toUpperCase();

        WorkingTimeSlot workingSlot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(workingTimeSlotRepository.findByEmployeeIdAndDayOfWeek(eq(employeeId), any()))
                .thenReturn(List.of(workingSlot));
    }
}

package beauty_center.modules.scheduling.service;

import beauty_center.modules.scheduling.dto.WorkingTimeSlotRequest;
import beauty_center.modules.scheduling.dto.WorkingTimeSlotResponse;
import beauty_center.modules.scheduling.entity.WorkingTimeSlot;
import beauty_center.modules.scheduling.repository.WorkingTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Working time slot service managing employee weekly schedules.
 * Handles validation for time slot overlaps and time consistency.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkingTimeSlotService {

    private final WorkingTimeSlotRepository workingTimeSlotRepository;

    /**
     * Get all working time slots for an employee
     */
    public List<WorkingTimeSlotResponse> getEmployeeWorkingTimes(UUID employeeId) {
        log.debug("Fetching working time slots for employee: {}", employeeId);
        List<WorkingTimeSlot> slots = workingTimeSlotRepository.findByEmployeeId(employeeId);
        return slots.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Replace all working time slots for an employee with new schedule.
     * This is a full replacement operation - deletes existing and creates new.
     */
    public List<WorkingTimeSlotResponse> replaceEmployeeWorkingTimes(
        UUID employeeId,
        List<WorkingTimeSlotRequest> requests
    ) {
        log.info("Replacing working time slots for employee: {}", employeeId);

        // Validate all slots before making changes
        for (WorkingTimeSlotRequest request : requests) {
            validateWorkingTimeSlot(request);
        }

        // Check for overlaps within the new schedule
        validateNoOverlapsInNewSchedule(requests);

        // Delete existing slots
        workingTimeSlotRepository.deleteByEmployeeId(employeeId);
        log.debug("Deleted existing working time slots for employee: {}", employeeId);

        // Create new slots
        List<WorkingTimeSlot> newSlots = requests.stream()
            .map(request -> createWorkingTimeSlot(employeeId, request))
            .collect(Collectors.toList());

        List<WorkingTimeSlot> saved = workingTimeSlotRepository.saveAll(newSlots);
        log.info("Created {} new working time slots for employee: {}", saved.size(), employeeId);

        return saved.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Validate a single working time slot
     */
    private void validateWorkingTimeSlot(WorkingTimeSlotRequest request) {
        // Validate end time is after start time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException(
                String.format("End time (%s) must be after start time (%s) for %s",
                    request.getEndTime(), request.getStartTime(), request.getDayOfWeek())
            );
        }
    }

    /**
     * Validate no overlaps exist within the new schedule being created
     */
    private void validateNoOverlapsInNewSchedule(List<WorkingTimeSlotRequest> requests) {
        for (int i = 0; i < requests.size(); i++) {
            WorkingTimeSlotRequest slot1 = requests.get(i);
            for (int j = i + 1; j < requests.size(); j++) {
                WorkingTimeSlotRequest slot2 = requests.get(j);

                // Only check overlaps for same day
                if (slot1.getDayOfWeek().equals(slot2.getDayOfWeek())) {
                    // Check if slots overlap
                    boolean overlaps = slot1.getStartTime().isBefore(slot2.getEndTime()) &&
                                     slot1.getEndTime().isAfter(slot2.getStartTime());

                    if (overlaps) {
                        throw new IllegalArgumentException(
                            String.format("Overlapping time slots detected for %s: %s-%s and %s-%s",
                                slot1.getDayOfWeek(),
                                slot1.getStartTime(), slot1.getEndTime(),
                                slot2.getStartTime(), slot2.getEndTime())
                        );
                    }
                }
            }
        }
    }

    /**
     * Create a working time slot entity from request
     */
    private WorkingTimeSlot createWorkingTimeSlot(UUID employeeId, WorkingTimeSlotRequest request) {
        return WorkingTimeSlot.builder()
            .employeeId(employeeId)
            .dayOfWeek(request.getDayOfWeek())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .build();
    }

    /**
     * Map entity to response DTO
     */
    private WorkingTimeSlotResponse mapToResponse(WorkingTimeSlot slot) {
        return WorkingTimeSlotResponse.builder()
            .id(slot.getId())
            .employeeId(slot.getEmployeeId())
            .dayOfWeek(slot.getDayOfWeek())
            .startTime(slot.getStartTime())
            .endTime(slot.getEndTime())
            .createdAt(slot.getCreatedAt())
            .updatedAt(slot.getUpdatedAt())
            .build();
    }

}


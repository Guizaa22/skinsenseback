package beauty_center.modules.scheduling.controller;

import beauty_center.modules.scheduling.dto.*;
import beauty_center.modules.scheduling.service.AbsenceService;
import beauty_center.modules.scheduling.service.EmployeeService;
import beauty_center.modules.scheduling.service.WorkingTimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller for employee management.
 * Handles employee accounts, working time slots, and absences.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final WorkingTimeSlotService workingTimeSlotService;
    private final AbsenceService absenceService;

    // ========== Employee Account Management ==========

    /**
     * Create new employee account
     * POST /api/admin/employees
     */
    @PostMapping("/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(
        @Valid @RequestBody EmployeeCreateRequest request
    ) {
        log.info("Admin creating employee: {}", request.getEmail());
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all employees with optional filters
     * GET /api/admin/employees?isActive=true
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
        @RequestParam(required = false) Boolean isActive
    ) {
        log.info("Admin fetching employees (isActive: {})", isActive);
        List<EmployeeResponse> employees = employeeService.getAllEmployees(isActive);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID
     * GET /api/admin/employees/{id}
     */
    @GetMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable UUID id) {
        log.info("Admin fetching employee: {}", id);
        return employeeService.getEmployeeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update employee information
     * PUT /api/admin/employees/{id}
     */
    @PutMapping("/employees/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable UUID id,
        @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        log.info("Admin updating employee: {}", id);
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    // ========== Working Time Slot Management ==========

    /**
     * Get employee working time slots
     * GET /api/admin/employees/{id}/working-times
     */
    @GetMapping("/employees/{id}/working-times")
    public ResponseEntity<List<WorkingTimeSlotResponse>> getEmployeeWorkingTimes(
        @PathVariable UUID id
    ) {
        log.info("Admin fetching working times for employee: {}", id);
        List<WorkingTimeSlotResponse> slots = workingTimeSlotService.getEmployeeWorkingTimes(id);
        return ResponseEntity.ok(slots);
    }

    /**
     * Replace employee's full weekly schedule
     * PUT /api/admin/employees/{id}/working-times
     */
    @PutMapping("/employees/{id}/working-times")
    public ResponseEntity<List<WorkingTimeSlotResponse>> replaceEmployeeWorkingTimes(
        @PathVariable UUID id,
        @Valid @RequestBody List<WorkingTimeSlotRequest> requests
    ) {
        log.info("Admin replacing working times for employee: {} ({} slots)", id, requests.size());
        List<WorkingTimeSlotResponse> slots = workingTimeSlotService.replaceEmployeeWorkingTimes(id, requests);
        return ResponseEntity.ok(slots);
    }

    // ========== Absence Management ==========

    /**
     * Get employee absences
     * GET /api/admin/employees/{id}/absences
     */
    @GetMapping("/employees/{id}/absences")
    public ResponseEntity<List<AbsenceResponse>> getEmployeeAbsences(@PathVariable UUID id) {
        log.info("Admin fetching absences for employee: {}", id);
        List<AbsenceResponse> absences = absenceService.getEmployeeAbsences(id);
        return ResponseEntity.ok(absences);
    }

    /**
     * Create absence for employee
     * POST /api/admin/employees/{id}/absences
     */
    @PostMapping("/employees/{id}/absences")
    public ResponseEntity<AbsenceResponse> createAbsence(
        @PathVariable UUID id,
        @Valid @RequestBody AbsenceCreateRequest request
    ) {
        log.info("Admin creating absence for employee: {}", id);
        AbsenceResponse response = absenceService.createAbsence(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete absence
     * DELETE /api/admin/absences/{absenceId}
     */
    @DeleteMapping("/absences/{absenceId}")
    public ResponseEntity<Void> deleteAbsence(@PathVariable UUID absenceId) {
        log.info("Admin deleting absence: {}", absenceId);
        absenceService.deleteAbsence(absenceId);
        return ResponseEntity.noContent().build();
    }

}


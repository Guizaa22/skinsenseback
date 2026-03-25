package beauty_center.modules.scheduling.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.scheduling.dto.EmployeeResponse;
import beauty_center.modules.scheduling.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class PublicEmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getActiveEmployees() {
        log.info("GET /api/employees - fetching active employees for booking");
        List<EmployeeResponse> employees = employeeService.getAllEmployees(true);
        return ResponseEntity.ok(ApiResponse.ok(employees, "Active employees retrieved"));
    }
}

package beauty_center.modules.scheduling;

import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.scheduling.dto.*;
import beauty_center.modules.users.entity.AuthProvider;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminEmployeeController.
 * Tests employee management, working time slots, and absences.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Employee Management Integration Tests")
@Slf4j
class AdminEmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create test users
        createTestUser("admin@test.com", "Admin@123", Role.ADMIN);
        createTestUser("employee@test.com", "Employee@123", Role.EMPLOYEE);

        // Login and get tokens
        adminToken = loginAndGetToken("admin@test.com", "Admin@123");
        employeeToken = loginAndGetToken("employee@test.com", "Employee@123");
    }

    // ========== Employee Account Tests ==========

    @Test
    @DisplayName("Should create employee successfully with admin role")
    void testCreateEmployeeSuccess() throws Exception {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
            .fullName("New Employee")
            .email("newemployee@test.com")
            .phone("+1-555-1234")
            .password("Password@123")
            .build();

        mockMvc.perform(post("/api/admin/employees")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.fullName").value("New Employee"))
            .andExpect(jsonPath("$.email").value("newemployee@test.com"))
            .andExpect(jsonPath("$.role").value("EMPLOYEE"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject employee creation with duplicate email")
    void testCreateEmployeeDuplicateEmail() throws Exception {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
            .fullName("Duplicate Employee")
            .email("employee@test.com") // Already exists
            .phone("+1-555-5678")
            .password("Password@123")
            .build();

        mockMvc.perform(post("/api/admin/employees")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject employee creation with invalid password")
    void testCreateEmployeeInvalidPassword() throws Exception {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
            .fullName("Test Employee")
            .email("test@test.com")
            .phone("+1-555-9999")
            .password("short") // Too short
            .build();

        mockMvc.perform(post("/api/admin/employees")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should deny employee creation without admin role")
    void testCreateEmployeeForbiddenForNonAdmin() throws Exception {
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
            .fullName("Test Employee")
            .email("test@test.com")
            .password("Password@123")
            .build();

        mockMvc.perform(post("/api/admin/employees")
            .header("Authorization", "Bearer " + employeeToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all employees")
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].role").value("EMPLOYEE"));
    }

    @Test
    @DisplayName("Should filter employees by active status")
    void testGetEmployeesByActiveStatus() throws Exception {
        mockMvc.perform(get("/api/admin/employees?active=true")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should update employee successfully")
    void testUpdateEmployeeSuccess() throws Exception {
        // First create an employee
        UUID employeeId = createTestEmployee("update@test.com");

        EmployeeUpdateRequest request = EmployeeUpdateRequest.builder()
            .fullName("Updated Name")
            .phone("+1-555-9999")
            .build();

        mockMvc.perform(put("/api/admin/employees/" + employeeId)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"))
            .andExpect(jsonPath("$.phone").value("+1-555-9999"));
    }

    @Test
    @DisplayName("Should deactivate employee")
    void testDeactivateEmployee() throws Exception {
        UUID employeeId = createTestEmployee("deactivate@test.com");

        EmployeeUpdateRequest request = EmployeeUpdateRequest.builder()
            .isActive(false)
            .build();

        mockMvc.perform(put("/api/admin/employees/" + employeeId)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    // ========== Working Time Slot Tests ==========

    @Test
    @DisplayName("Should replace employee working times successfully")
    void testReplaceWorkingTimesSuccess() throws Exception {
        UUID employeeId = createTestEmployee("schedule@test.com");

        List<WorkingTimeSlotRequest> slots = Arrays.asList(
            WorkingTimeSlotRequest.builder()
                .dayOfWeek("MON")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build(),
            WorkingTimeSlotRequest.builder()
                .dayOfWeek("TUE")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build()
        );

        mockMvc.perform(put("/api/admin/employees/" + employeeId + "/working-times")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slots)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].dayOfWeek").value("MON"))
            .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
            .andExpect(jsonPath("$[0].endTime").value("17:00:00"));
    }

    @Test
    @DisplayName("Should reject overlapping working time slots")
    void testReplaceWorkingTimesOverlap() throws Exception {
        UUID employeeId = createTestEmployee("overlap@test.com");

        List<WorkingTimeSlotRequest> slots = Arrays.asList(
            WorkingTimeSlotRequest.builder()
                .dayOfWeek("MON")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build(),
            WorkingTimeSlotRequest.builder()
                .dayOfWeek("MON")
                .startTime(LocalTime.of(12, 0)) // Overlaps with previous
                .endTime(LocalTime.of(17, 0))
                .build()
        );

        mockMvc.perform(put("/api/admin/employees/" + employeeId + "/working-times")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slots)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject invalid time slot (end before start)")
    void testReplaceWorkingTimesInvalidTime() throws Exception {
        UUID employeeId = createTestEmployee("invalidtime@test.com");

        List<WorkingTimeSlotRequest> slots = Collections.singletonList(
                WorkingTimeSlotRequest.builder()
                        .dayOfWeek("MON")
                        .startTime(LocalTime.of(17, 0))
                        .endTime(LocalTime.of(9, 0)) // End before start
                        .build()
        );

        mockMvc.perform(put("/api/admin/employees/" + employeeId + "/working-times")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(slots)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get employee working times")
    void testGetWorkingTimes() throws Exception {
        UUID employeeId = createTestEmployee("getschedule@test.com");

        mockMvc.perform(get("/api/admin/employees/" + employeeId + "/working-times")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }


    // ========== Absence Tests ==========

    @Test
    @DisplayName("Should create absence successfully")
    void testCreateAbsenceSuccess() throws Exception {
        UUID employeeId = createTestEmployee("absence@test.com");

        AbsenceCreateRequest request = AbsenceCreateRequest.builder()
            .startAt(OffsetDateTime.now().plusDays(1))
            .endAt(OffsetDateTime.now().plusDays(3))
            .reason("Vacation")
            .build();

        mockMvc.perform(post("/api/admin/employees/" + employeeId + "/absences")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
            .andExpect(jsonPath("$.reason").value("Vacation"));
    }

    @Test
    @DisplayName("Should reject absence with invalid dates (end before start)")
    void testCreateAbsenceInvalidDates() throws Exception {
        UUID employeeId = createTestEmployee("invalidabsence@test.com");

        AbsenceCreateRequest request = AbsenceCreateRequest.builder()
            .startAt(OffsetDateTime.now().plusDays(3))
            .endAt(OffsetDateTime.now().plusDays(1)) // End before start
            .reason("Invalid")
            .build();

        mockMvc.perform(post("/api/admin/employees/" + employeeId + "/absences")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get employee absences")
    void testGetEmployeeAbsences() throws Exception {
        UUID employeeId = createTestEmployee("getabsences@test.com");

        mockMvc.perform(get("/api/admin/employees/" + employeeId + "/absences")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should delete absence successfully")
    void testDeleteAbsenceSuccess() throws Exception {
        UUID employeeId = createTestEmployee("deleteabsence@test.com");

        // Create absence first
        AbsenceCreateRequest createRequest = AbsenceCreateRequest.builder()
            .startAt(OffsetDateTime.now().plusDays(1))
            .endAt(OffsetDateTime.now().plusDays(2))
            .reason("Test")
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/admin/employees/" + employeeId + "/absences")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        UUID absenceId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        // Delete absence
        mockMvc.perform(delete("/api/admin/absences/" + absenceId)
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNoContent());
    }

    // ========== Helper Methods ==========

    private void createTestUser(String email, String password, Role role) {
        if (userAccountRepository.findByEmail(email).isEmpty()) {
            UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .fullName("Test " + role.name())
                .email(email)
                .phone("+1-555-0000")
                .passwordHash(passwordEncoder.encode(password))
                .active(true)
                .role(role)
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

            userAccountRepository.save(user);
        }
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email(email)
            .password(password)
            .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody)
            .get("data").get("accessToken").asText();
    }

    private UUID createTestEmployee(String email) {
        UserAccount employee = UserAccount.builder()
            .id(UUID.randomUUID())
            .fullName("Test Employee")
            .email(email)
            .phone("+1-555-1111")
            .passwordHash(passwordEncoder.encode("Password@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .provider(AuthProvider.LOCAL)
            .emailVerified(false)
            .build();

        return userAccountRepository.save(employee).getId();
    }

}



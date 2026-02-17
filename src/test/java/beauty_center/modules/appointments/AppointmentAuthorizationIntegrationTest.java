package beauty_center.modules.appointments;

import beauty_center.modules.appointments.dto.AppointmentUpdateRequest;
import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.BeautyServiceEmployee;
import beauty_center.modules.services.repository.BeautyServiceEmployeeRepository;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Appointment Authorization Integration Tests")
class AppointmentAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BeautyServiceRepository beautyServiceRepository;

    @Autowired
    private BeautyServiceEmployeeRepository beautyServiceEmployeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token1;
    private String token2;
    private String adminToken;
    private UUID clientId;
    private UUID employee1Id;
    private UUID employee2Id;
    private UUID serviceId;
    private UUID appointmentId;

    @BeforeEach
    void setup() throws Exception {
        createTestUsers();
        createTestService();
        createTestAppointment();
    }

    private void createTestUsers() throws Exception {
        // Create client
        UserAccount client = UserAccount.builder()
            .fullName("Test Client")
            .email("auth-client@test.com")
            .passwordHash(passwordEncoder.encode("Client@123"))
            .active(true)
            .role(Role.CLIENT)
            .build();
        clientId = userAccountRepository.save(client).getId();

        // Create employee 1
        UserAccount emp1 = UserAccount.builder()
            .fullName("Employee 1")
            .email("auth-emp1@test.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .build();
        employee1Id = userAccountRepository.save(emp1).getId();

        // Create employee 2
        UserAccount emp2 = UserAccount.builder()
            .fullName("Employee 2")
            .email("auth-emp2@test.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .build();
        employee2Id = userAccountRepository.save(emp2).getId();

        // Create admin
        UserAccount admin = UserAccount.builder()
            .fullName("Admin User")
            .email("auth-admin@test.com")
            .passwordHash(passwordEncoder.encode("Admin@123"))
            .active(true)
            .role(Role.ADMIN)
            .build();
        userAccountRepository.save(admin);

        // Get tokens for all users
        token1 = loginAndGetToken("auth-emp1@test.com", "Employee@123");
        token2 = loginAndGetToken("auth-emp2@test.com", "Employee@123");
        adminToken = loginAndGetToken("auth-admin@test.com", "Admin@123");
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

    private void createTestService() {
        BeautyService service = BeautyService.builder()
            .name("Test Auth Service")
            .description("60-minute test service")
            .durationMin(60)
            .price(BigDecimal.valueOf(50.0))
            .isActive(true)
            .build();
        serviceId = beautyServiceRepository.save(service).getId();

        beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
            .beautyServiceId(serviceId)
            .employeeId(employee1Id)
            .build());

        beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
            .beautyServiceId(serviceId)
            .employeeId(employee2Id)
            .build());
    }

    private void createTestAppointment() {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        OffsetDateTime startTime = today.atTime(10, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        Appointment appointment = Appointment.builder()
            .clientId(clientId)
            .employeeId(employee1Id)
            .beautyServiceId(serviceId)
            .startAt(startTime)
            .endAt(startTime.plusMinutes(60))
            .status(AppointmentStatus.CONFIRMED)
            .build();

        appointmentId = appointmentRepository.save(appointment).getId();
    }

    @Test
    @DisplayName("Employee assigned to appointment can complete it")
    void testAssignedEmployeeCanComplete() throws Exception {
        mockMvc.perform(post("/api/appointments/" + appointmentId + "/complete")
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Different employee cannot complete appointment")
    void testDifferentEmployeeCannotComplete() throws Exception {
        mockMvc.perform(post("/api/appointments/" + appointmentId + "/complete")
            .header("Authorization", "Bearer " + token2)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Admin can complete any appointment")
    void testAdminCanComplete() throws Exception {
        mockMvc.perform(post("/api/appointments/" + appointmentId + "/complete")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Employee assigned to appointment can reschedule it")
    void testAssignedEmployeeCanReschedule() throws Exception {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Africa/Tunis")).plusDays(1);
        OffsetDateTime newTime = tomorrow.atTime(14, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentUpdateRequest req = AppointmentUpdateRequest.builder()
            .employeeId(employee1Id)
            .serviceId(serviceId)
            .startAt(newTime)
            .build();

        mockMvc.perform(put("/api/appointments/" + appointmentId)
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Different employee cannot reschedule appointment")
    void testDifferentEmployeeCannotReschedule() throws Exception {
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Africa/Tunis")).plusDays(1);
        OffsetDateTime newTime = tomorrow.atTime(14, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentUpdateRequest req = AppointmentUpdateRequest.builder()
            .employeeId(employee2Id)
            .serviceId(serviceId)
            .startAt(newTime)
            .build();

        mockMvc.perform(put("/api/appointments/" + appointmentId)
            .header("Authorization", "Bearer " + token2)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success", is(false)));
    }
}


package beauty_center.modules.appointments;

import beauty_center.modules.appointments.dto.AppointmentCreateRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Employee-Service Validation Integration Tests")
class EmployeeServiceValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BeautyServiceRepository beautyServiceRepository;

    @Autowired
    private BeautyServiceEmployeeRepository beautyServiceEmployeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String clientToken;
    private UUID clientId;
    private UUID allowedEmployeeId;
    private UUID forbiddenEmployeeId;
    private UUID serviceId;

    @BeforeEach
    void setup() throws Exception {
        createTestUsers();
        createTestService();
    }

    private void createTestUsers() throws Exception {
        UserAccount client = UserAccount.builder()
            .fullName("Service Validation Client")
            .email("sv-client@test.com")
            .passwordHash(passwordEncoder.encode("Client@123"))
            .active(true)
            .role(Role.CLIENT)
            .build();
        clientId = userAccountRepository.save(client).getId();

        UserAccount emp1 = UserAccount.builder()
            .fullName("Allowed Employee")
            .email("sv-allowed@test.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .build();
        allowedEmployeeId = userAccountRepository.save(emp1).getId();

        UserAccount emp2 = UserAccount.builder()
            .fullName("Forbidden Employee")
            .email("sv-forbidden@test.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .build();
        forbiddenEmployeeId = userAccountRepository.save(emp2).getId();

        // Get client token
        clientToken = loginAndGetToken("sv-client@test.com", "Client@123");
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
            .name("Specialized Service")
            .description("Only certain employees can do this")
            .durationMin(60)
            .price(BigDecimal.valueOf(100.0))
            .isActive(true)
            .build();
        serviceId = beautyServiceRepository.save(service).getId();

        beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
            .beautyServiceId(serviceId)
            .employeeId(allowedEmployeeId)
            .build());
    }

    @Test
    @DisplayName("Should allow appointment when employee is allowed for service")
    void testAllowedEmployeeCanBookService() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        OffsetDateTime startTime = today.atTime(10, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentCreateRequest req = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .employeeId(allowedEmployeeId)
            .serviceId(serviceId)
            .startAt(startTime)
            .notes("Valid service booking")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Should reject appointment when employee is not allowed for service")
    void testForbiddenEmployeeCannotBookService() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        OffsetDateTime startTime = today.atTime(10, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentCreateRequest req = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .employeeId(forbiddenEmployeeId)
            .serviceId(serviceId)
            .startAt(startTime)
            .notes("Invalid service booking")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message").value("Employee is not authorized to perform this service: " + serviceId));
    }
}


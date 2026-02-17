package beauty_center.modules.appointments;

import beauty_center.modules.appointments.dto.AppointmentCreateRequest;
import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.scheduling.entity.WorkingTimeSlot;
import beauty_center.modules.scheduling.repository.WorkingTimeSlotRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
@DisplayName("Appointment Back-to-Back Integration Tests")
class AppointmentBackToBackIntegrationTest {

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
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String clientToken;
    private UUID clientId;
    private UUID employeeId;
    private UUID serviceId;

    @BeforeEach
    void setup() throws Exception {
        createTestUsers();
        createTestService();
        setupWorkingHours();
    }

    private void createTestUsers() throws Exception {
        UserAccount client = UserAccount.builder()
            .fullName("Test Client")
            .email("backtests@test.com")
            .passwordHash(passwordEncoder.encode("Client@123"))
            .active(true)
            .role(Role.CLIENT)
            .build();
        clientId = userAccountRepository.save(client).getId();

        UserAccount employee = UserAccount.builder()
            .fullName("Test Employee")
            .email("backtest-emp@test.com")
            .passwordHash(passwordEncoder.encode("Employee@123"))
            .active(true)
            .role(Role.EMPLOYEE)
            .build();
        employeeId = userAccountRepository.save(employee).getId();

        // Get tokens
        clientToken = loginAndGetToken("backtests@test.com", "Client@123");
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

    // ...existing code...

    private void createTestService() {
        BeautyService service = BeautyService.builder()
            .name("Test Service")
            .description("60-minute test service")
            .durationMin(60)
            .price(java.math.BigDecimal.valueOf(50.0))
            .isActive(true)
            .build();
        serviceId = beautyServiceRepository.save(service).getId();

        BeautyServiceEmployee assignment = BeautyServiceEmployee.builder()
            .beautyServiceId(serviceId)
            .employeeId(employeeId)
            .build();
        beautyServiceEmployeeRepository.save(assignment);
    }

    private void setupWorkingHours() {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        String dayOfWeek = today.getDayOfWeek().toString().substring(0, 3).toUpperCase();

        WorkingTimeSlot slot = WorkingTimeSlot.builder()
            .employeeId(employeeId)
            .dayOfWeek(dayOfWeek)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(18, 0))
            .build();
        workingTimeSlotRepository.save(slot);
    }

    @Test
    @DisplayName("Should allow back-to-back appointments (10:00-11:00 and 11:00-12:00)")
    void testBackToBackAppointmentsAllowed() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        OffsetDateTime slot1Start = today.atTime(10, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();
        OffsetDateTime slot2Start = today.atTime(11, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentCreateRequest req1 = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .serviceId(serviceId)
            .startAt(slot1Start)
            .notes("First appointment")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)));

        AppointmentCreateRequest req2 = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .serviceId(serviceId)
            .startAt(slot2Start)
            .notes("Second appointment")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)));

        long count = appointmentRepository.findAll().stream()
            .filter(a -> a.getEmployeeId().equals(employeeId))
            .count();
        assert count == 2 : "Should have 2 appointments";
    }

    @Test
    @DisplayName("Should reject overlapping appointments (10:00-11:00 and 10:30-11:30)")
    void testOverlappingAppointmentsRejected() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Tunis"));
        OffsetDateTime slot1Start = today.atTime(10, 0).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();
        OffsetDateTime slot2Start = today.atTime(10, 30).atZone(ZoneId.of("Africa/Tunis")).toOffsetDateTime();

        AppointmentCreateRequest req1 = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .serviceId(serviceId)
            .startAt(slot1Start)
            .notes("First appointment")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        AppointmentCreateRequest req2 = AppointmentCreateRequest.builder()
            .clientId(clientId)
            .serviceId(serviceId)
            .startAt(slot2Start)
            .notes("Overlapping appointment")
            .build();

        mockMvc.perform(post("/api/appointments")
            .header("Authorization", "Bearer " + clientToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success", is(false)));
    }
}


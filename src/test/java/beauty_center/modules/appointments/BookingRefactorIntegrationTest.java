package beauty_center.modules.appointments;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.scheduling.dto.AvailabilityResponse;
import beauty_center.modules.services.dto.BeautyServiceResponse;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.BeautyServiceEmployee;
import beauty_center.modules.services.repository.BeautyServiceEmployeeRepository;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Booking Refactor Integration Tests")
class BookingRefactorIntegrationTest {

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
        private beauty_center.modules.appointments.repository.AppointmentRepository appointmentRepository;

        @Autowired
        private beauty_center.modules.scheduling.repository.WorkingTimeSlotRepository workingTimeSlotRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private UUID serviceId;
        private UUID employeeId;

        private String token;

        @BeforeEach
        void setup() throws Exception {
                createTestUsersAndService();
                token = loginAndGetToken("client@test.com", "client123");
        }

        private void createTestUsersAndService() {
                // Create Client
                UserAccount client = UserAccount.builder()
                                .fullName("Test Client")
                                .email("client@test.com")
                                .passwordHash(passwordEncoder.encode("client123"))
                                .role(Role.CLIENT)
                                .active(true)
                                .build();
                userAccountRepository.save(client);

                UserAccount employee = UserAccount.builder()
                                .fullName("Test Employee")
                                .email("employee@test.com")
                                .passwordHash(passwordEncoder.encode("test"))
                                .role(Role.EMPLOYEE)
                                .active(true)
                                .build();
                employeeId = userAccountRepository.save(employee).getId();

                BeautyService service = BeautyService.builder()
                                .name("Test Service")
                                .description("Description")
                                .durationMin(60)
                                .durationMin(60)
                                .price(BigDecimal.valueOf(100))
                                .isActive(true)
                                .build();
                serviceId = beautyServiceRepository.save(service).getId();

                beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
                                .beautyServiceId(serviceId)
                                .employeeId(employeeId)
                                .build());
        }

        private String loginAndGetToken(String email, String password) throws Exception {
                beauty_center.modules.auth.dto.LoginRequest request = beauty_center.modules.auth.dto.LoginRequest
                                .builder()
                                .email(email)
                                .password(password)
                                .build();

                MvcResult result = mockMvc
                                .perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                                .post("/api/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                return objectMapper.readTree(responseBody)
                                .get("data").get("accessToken").asText();
        }

        @Test
        @DisplayName("Public user cannot see allowedEmployeeIds in service response")
        void testPublicServiceAccessHidesEmployees() throws Exception {
                // Public access (no token)
                MvcResult result = mockMvc.perform(get("/api/services/" + serviceId))
                                .andExpect(status().isOk())
                                .andReturn();

                String content = result.getResponse().getContentAsString();
                ApiResponse<BeautyServiceResponse> response = objectMapper.readValue(
                                content, new TypeReference<>() {
                                });

                assertNotNull(response.getData());
                assertNull(response.getData().getAllowedEmployeeIds(), "Employee IDs should be null for public users");
        }

        @Test
        @DisplayName("Availability endpoint works without employeeId")
        void testAvailabilityWithoutEmployeeId() throws Exception {
                LocalDate today = LocalDate.now();
                mockMvc.perform(get("/api/availability")
                                .param("serviceId", serviceId.toString())
                                .param("date", today.toString())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.availableSlots").isArray());
        }

        @Test
        @DisplayName("Admin can reassign appointment")
        void testAdminReassignAppointment() throws Exception {
                // Create admin user
                UserAccount admin = UserAccount.builder()
                                .fullName("Test Admin")
                                .email("testadmin@test.com")
                                .passwordHash(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .active(true)
                                .build();
                userAccountRepository.save(admin);

                // Create admin token
                String adminToken = loginAndGetToken("testadmin@test.com", "admin123");
                // admin

                // Create another employee
                UserAccount employee2 = UserAccount.builder()
                                .fullName("Employee 2")
                                .email("employee2@test.com")
                                .passwordHash(passwordEncoder.encode("test"))
                                .role(Role.EMPLOYEE)
                                .active(true)
                                .build();
                UUID employee2Id = userAccountRepository.save(employee2).getId();

                beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
                                .beautyServiceId(serviceId)
                                .employeeId(employee2Id)
                                .build());

                // Add working hours for employee2 for tomorrow
                java.time.DayOfWeek tomorrowDay = java.time.LocalDate.now().plusDays(1).getDayOfWeek();
                String dayOfWeekStr = tomorrowDay
                                .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                                .toUpperCase();
                workingTimeSlotRepository.save(beauty_center.modules.scheduling.entity.WorkingTimeSlot.builder()
                                .employeeId(employee2Id)
                                .dayOfWeek(dayOfWeekStr)
                                .startTime(java.time.LocalTime.of(9, 0))
                                .endTime(java.time.LocalTime.of(17, 0))
                                .build());

                // Create an appointment for employee 1
                beauty_center.modules.appointments.entity.Appointment appointment = beauty_center.modules.appointments.entity.Appointment
                                .builder()
                                .clientId(UUID.randomUUID()) // Mock client
                                .employeeId(employeeId)
                                .beautyServiceId(serviceId)
                                .startAt(java.time.OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0))
                                .endAt(java.time.OffsetDateTime.now().plusDays(1).withHour(11).withMinute(0))
                                .status(beauty_center.modules.appointments.entity.AppointmentStatus.CONFIRMED)
                                .build();
                appointment = appointmentRepository.save(appointment);

                // Reassign to employee 2
                beauty_center.modules.appointments.dto.AppointmentReassignRequest request = new beauty_center.modules.appointments.dto.AppointmentReassignRequest();
                request.setEmployeeId(employee2Id);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/appointments/" + appointment.getId() + "/reassign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.employeeId").value(employee2Id.toString()));

                // Verify in DB
                beauty_center.modules.appointments.entity.Appointment updated = appointmentRepository
                                .findById(appointment.getId()).orElseThrow();
                assertEquals(employee2Id, updated.getEmployeeId());
        }

        @Test
        @DisplayName("Client cannot reassign appointment")
        void testClientCannotReassignAppointment() throws Exception {
                // Create appointment
                beauty_center.modules.appointments.entity.Appointment appointment = beauty_center.modules.appointments.entity.Appointment
                                .builder()
                                .clientId(UUID.randomUUID())
                                .employeeId(employeeId)
                                .beautyServiceId(serviceId)
                                .startAt(java.time.OffsetDateTime.now().plusDays(2).withHour(10).withMinute(0))
                                .endAt(java.time.OffsetDateTime.now().plusDays(2).withHour(11).withMinute(0))
                                .status(beauty_center.modules.appointments.entity.AppointmentStatus.CONFIRMED)
                                .build();
                appointment = appointmentRepository.save(appointment);

                // Try to reassign as client (token is client token)
                beauty_center.modules.appointments.dto.AppointmentReassignRequest request = new beauty_center.modules.appointments.dto.AppointmentReassignRequest();
                request.setEmployeeId(employeeId); // Doesn't matter, should fail auth

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/appointments/" + appointment.getId() + "/reassign")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isForbidden());
        }
}

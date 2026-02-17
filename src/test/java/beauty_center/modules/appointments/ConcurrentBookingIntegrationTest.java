package beauty_center.modules.appointments;

import beauty_center.modules.appointments.dto.AppointmentCreateRequest;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.scheduling.dto.WorkingTimeSlotRequest;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for concurrent booking scenarios.
 * Verifies that when two clients try to book the same time slot simultaneously,
 * one succeeds with 201 Created and the other fails with 409 Conflict.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Concurrent Booking Integration Tests")
@Slf4j
class ConcurrentBookingIntegrationTest {

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
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String clientToken1;
    private String clientToken2;
    private UUID client1Id;
    private UUID client2Id;
    private UUID employeeId;
    private UUID serviceId;

    @BeforeEach
    void setUp() throws Exception {
        log.info("Setting up concurrent booking test");

        // Create two client users
        client1Id = createTestUser("client1@test.com", "Client1@123", Role.CLIENT);
        client2Id = createTestUser("client2@test.com", "Client2@123", Role.CLIENT);

        // Create employee user
        employeeId = createTestUser("employee@test.com", "Employee@123", Role.EMPLOYEE);

        // Create employee working hours (Monday 09:00-17:00)
        createWorkingHours(employeeId, "MON", LocalTime.of(9, 0), LocalTime.of(17, 0));

        // Create a beauty service (60 minutes)
        serviceId = createBeautyService("Facial Treatment", 60, new BigDecimal("50.00"));

        // Login and get tokens for both clients
        clientToken1 = loginAndGetToken("client1@test.com", "Client1@123");
        clientToken2 = loginAndGetToken("client2@test.com", "Client2@123");

        log.info("Test setup complete - Employee: {}, Service: {}", employeeId, serviceId);
    }

    @Test
    @DisplayName("Should handle concurrent bookings - one succeeds with 201, one fails with 409")
    void testConcurrentBookings() throws Exception {
        log.info("Starting concurrent booking test");

        // Choose a Monday in the future for the appointment
        OffsetDateTime appointmentTime = getNextMonday().withHour(10).withMinute(0).withSecond(0).withNano(0);

        log.info("Attempting to book slot at: {}", appointmentTime);

        // Create identical booking requests for both clients
        AppointmentCreateRequest request1 = AppointmentCreateRequest.builder()
                .serviceId(serviceId)
                .startAt(appointmentTime)
                .notes("Client 1 booking")
                .build();

        AppointmentCreateRequest request2 = AppointmentCreateRequest.builder()
                .serviceId(serviceId)
                .startAt(appointmentTime)
                .notes("Client 2 booking")
                .build();

        // Use CountDownLatch to synchronize concurrent execution
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Submit first booking request
        executor.submit(() -> {
            try {
                startLatch.await(); // Wait for signal to start
                MvcResult result = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + clientToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                        .andReturn();

                int status = result.getResponse().getStatus();
                log.info("Client 1 received status: {}", status);

                if (status == 201) {
                    successCount.incrementAndGet();
                } else if (status == 409) {
                    conflictCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Client 1 request failed", e);
            } finally {
                doneLatch.countDown();
            }
        });

        // Submit second booking request
        executor.submit(() -> {
            try {
                startLatch.await(); // Wait for signal to start
                MvcResult result = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + clientToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                        .andReturn();

                int status = result.getResponse().getStatus();
                log.info("Client 2 received status: {}", status);

                if (status == 201) {
                    successCount.incrementAndGet();
                } else if (status == 409) {
                    conflictCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Client 2 request failed", e);
            } finally {
                doneLatch.countDown();
            }
        });

        // Start both requests simultaneously
        log.info("Releasing both concurrent requests");
        startLatch.countDown();

        // Wait for both requests to complete
        doneLatch.await();
        executor.shutdown();

        log.info("Concurrent booking test complete - Success: {}, Conflict: {}",
                successCount.get(), conflictCount.get());

        // Verify results: exactly one should succeed, one should fail with conflict
        assertThat(successCount.get()).isEqualTo(1)
                .withFailMessage("Expected exactly 1 successful booking, but got " + successCount.get());
        assertThat(conflictCount.get()).isEqualTo(1)
                .withFailMessage("Expected exactly 1 conflict (409), but got " + conflictCount.get());
    }

    // ========== Helper Methods ==========

    private UUID createTestUser(String email, String password, Role role) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            return userAccountRepository.findByEmail(email).get().getId();
        }

        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .fullName("Test " + role.name())
                .email(email)
                .phone("+1-555-0000")
                .passwordHash(passwordEncoder.encode(password))
                .active(true)
                .role(role)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        log.debug("Created test user: {} with role {}", email, role);
        return saved.getId();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody)
                .get("data")
                .get("accessToken")
                .asText();

        log.debug("Obtained token for user: {}", email);
        return token;
    }

    private void createWorkingHours(UUID employeeId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        WorkingTimeSlot slot = WorkingTimeSlot.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        workingTimeSlotRepository.save(slot);
        log.debug("Created working hours for employee {} on {}: {} - {}",
                employeeId, dayOfWeek, startTime, endTime);
    }

    private UUID createBeautyService(String name, int durationMin, BigDecimal price) {
        BeautyService service = BeautyService.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("Test service")
                .durationMin(durationMin)
                .price(price)
                .isActive(true)
                .build();

        BeautyService saved = beautyServiceRepository.save(service);

        // Authorize the employee to perform this service
        beautyServiceEmployeeRepository.save(BeautyServiceEmployee.builder()
                .beautyServiceId(saved.getId())
                .employeeId(employeeId)
                .build());

        log.debug("Created beauty service: {} ({} min, {})", name, durationMin, price);
        return saved.getId();
    }

    /**
     * Get the next Monday from now (Tunisia timezone UTC+1)
     */
    private OffsetDateTime getNextMonday() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(1));
        OffsetDateTime nextMonday = now.plusDays(1);

        // Find next Monday
        while (nextMonday.getDayOfWeek().getValue() != 1) { // 1 = Monday
            nextMonday = nextMonday.plusDays(1);
        }

        return nextMonday;
    }

}



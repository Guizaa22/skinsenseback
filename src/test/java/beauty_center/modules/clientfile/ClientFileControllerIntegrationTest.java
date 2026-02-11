package beauty_center.modules.clientfile;

import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.auth.dto.LoginResponse;
import beauty_center.modules.clientfile.dto.*;
import beauty_center.modules.clientfile.entity.ClientFile;
import beauty_center.modules.clientfile.repository.ClientFileRepository;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Client File endpoints.
 * Tests access control, CRUD operations, and audit logging.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ClientFileControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserAccountRepository userAccountRepository;

        @Autowired
        private ClientFileRepository clientFileRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private UserAccount testClient;
        private UserAccount testEmployee;
        private String clientToken;
        private String employeeToken;

        @BeforeEach
        void setUp() throws Exception {
                // Clean up
                clientFileRepository.deleteAll();
                userAccountRepository.deleteAll();

                // Create test client
                testClient = UserAccount.builder()
                                .id(UUID.randomUUID())
                                .fullName("Test Client")
                                .email("testclient@beautycenter.com")
                                .phone("+1-555-1111")
                                .passwordHash(passwordEncoder.encode("Client@123"))
                                .role(Role.CLIENT)
                                .active(true)
                                .build();
                userAccountRepository.save(testClient);

                // Create test employee
                testEmployee = UserAccount.builder()
                                .id(UUID.randomUUID())
                                .fullName("Test Employee")
                                .email("testemployee@beautycenter.com")
                                .phone("+1-555-2222")
                                .passwordHash(passwordEncoder.encode("Employee@123"))
                                .role(Role.EMPLOYEE)
                                .active(true)
                                .build();
                userAccountRepository.save(testEmployee);

                // Login as client
                clientToken = loginAndGetToken("testclient@beautycenter.com", "Client@123");

                // Login as employee
                employeeToken = loginAndGetToken("testemployee@beautycenter.com", "Employee@123");
        }

        @Test
        void testClientCanGetOwnFile() throws Exception {
                // When: Client gets their own file
                MvcResult result = mockMvc.perform(get("/api/client/me/file")
                                .header("Authorization", "Bearer " + clientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.clientId").value(testClient.getId().toString()))
                                .andReturn();

                // Then: File is created automatically if doesn't exist
                String response = result.getResponse().getContentAsString();
                assertThat(response).contains("Client file retrieved successfully");
        }

        @Test
        void testClientCanUpdateOwnFile() throws Exception {
                // Given: Update request
                ClientFileUpdateRequest request = ClientFileUpdateRequest.builder()
                                .intake(ClientIntakeDto.builder()
                                                .howDidYouHearAboutUs("Google")
                                                .consultationReason("Acne treatment")
                                                .objective("Clear skin")
                                                .build())
                                .medicalHistory(MedicalHistoryDto.builder()
                                                .allergiesAndReactions("Penicillin")
                                                .medicalBackground("Healthy")
                                                .build())
                                .photoConsentForFollowup(true)
                                .build();

                // When: Client updates their own file
                mockMvc.perform(put("/api/client/me/file")
                                .header("Authorization", "Bearer " + clientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.intake.howDidYouHearAboutUs").value("Google"))
                                .andExpect(jsonPath("$.data.intake.consultationReason").value("Acne treatment"))
                                .andExpect(jsonPath("$.data.medicalHistory.allergiesAndReactions").value("Penicillin"))
                                .andExpect(jsonPath("$.data.photoConsentForFollowup").value(true));

                // Then: File is persisted
                ClientFile saved = clientFileRepository.findByClientId(testClient.getId()).orElseThrow();
                assertThat(saved.getHowDidYouHearAboutUs()).isEqualTo("Google");
                assertThat(saved.getConsultationReason()).isEqualTo("Acne treatment");
                assertThat(saved.getAllergiesAndReactions()).isEqualTo("Penicillin");
                assertThat(saved.isPhotoConsentForFollowup()).isTrue();
        }

        @Test
        void testEmployeeCanReadClientFile() throws Exception {
                // Given: Client has a file
                ClientFile clientFile = ClientFile.builder()
                                .clientId(testClient.getId())
                                .howDidYouHearAboutUs("Referral")
                                .consultationReason("Skin care")
                                .allergiesAndReactions("None")
                                .photoConsentForFollowup(false)
                                .photoConsentForMarketing(false)
                                .build();
                clientFileRepository.save(clientFile);

                // When: Employee reads client file
                mockMvc.perform(get("/api/clients/" + testClient.getId() + "/file")
                                .header("Authorization", "Bearer " + employeeToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.clientId").value(testClient.getId().toString()))
                                .andExpect(jsonPath("$.data.intake.howDidYouHearAboutUs").value("Referral"))
                                .andExpect(jsonPath("$.data.intake.consultationReason").value("Skin care"));
        }

        @Test
        void testClientCannotAccessOtherClientFile() throws Exception {
                // Given: Another client
                UserAccount otherClient = UserAccount.builder()
                                .id(UUID.randomUUID())
                                .fullName("Other Client")
                                .email("otherclient@beautycenter.com")
                                .phone("+1-555-3333")
                                .passwordHash(passwordEncoder.encode("Client@123"))
                                .role(Role.CLIENT)
                                .active(true)
                                .build();
                userAccountRepository.save(otherClient);

                // When: Client tries to access other client's file
                mockMvc.perform(get("/api/clients/" + otherClient.getId() + "/file")
                                .header("Authorization", "Bearer " + clientToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testUnauthenticatedAccessDenied() throws Exception {
                // When: Unauthenticated request
                mockMvc.perform(get("/api/client/me/file"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(put("/api/client/me/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testEmployeeCannotAccessClientSelfServiceEndpoint() throws Exception {
                // When: Employee tries to use client self-service endpoint
                mockMvc.perform(get("/api/client/me/file")
                                .header("Authorization", "Bearer " + employeeToken))
                                .andExpect(status().isForbidden());
        }

        // Helper method to login and get JWT token
        private String loginAndGetToken(String email, String password) throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail(email);
                loginRequest.setPassword(password);

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
                return loginResponse.getAccessToken();
        }
}

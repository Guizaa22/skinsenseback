package beauty_center.modules.clientfile;

import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.clientfile.dto.ClientConsentUpdateRequest;
import beauty_center.modules.clientfile.entity.ClientConsent;
import beauty_center.modules.clientfile.repository.ClientConsentRepository;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
 * Integration tests for Client Consent endpoints.
 * Tests SMS opt-in/opt-out, unsubscribe token, and access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ClientConsentControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserAccountRepository userAccountRepository;

        @Autowired
        private ClientConsentRepository clientConsentRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private UserAccount testClient;
        private String clientToken;

        @BeforeEach
        void setUp() throws Exception {
                // Clean up
                clientConsentRepository.deleteAll();
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

                // Login as client
                clientToken = loginAndGetToken("testclient@beautycenter.com", "Client@123");
        }

        @Test
        void testClientCanGetOwnConsent() throws Exception {
                // When: Client gets their own consent
                MvcResult result = mockMvc.perform(get("/api/client/me/consent")
                                .header("Authorization", "Bearer " + clientToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.clientId").value(testClient.getId().toString()))
                                .andExpect(jsonPath("$.data.smsOptIn").value(true))
                                .andExpect(jsonPath("$.data.smsUnsubToken").exists())
                                .andReturn();

                // Then: Consent is created automatically with default values
                String response = result.getResponse().getContentAsString();
                assertThat(response).contains("Consent preferences retrieved successfully");

                // Verify token was generated
                ClientConsent saved = clientConsentRepository.findByClientId(testClient.getId()).orElseThrow();
                assertThat(saved.getSmsUnsubToken()).isNotNull();
                assertThat(saved.isSmsOptIn()).isTrue();
        }

        @Test
        void testClientCanUpdateConsent() throws Exception {
                // Given: Update request to opt-out
                ClientConsentUpdateRequest request = ClientConsentUpdateRequest.builder()
                                .smsOptIn(false)
                                .build();

                // When: Client updates their consent
                mockMvc.perform(put("/api/client/me/consent")
                                .header("Authorization", "Bearer " + clientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.smsOptIn").value(false));

                // Then: Consent is updated
                ClientConsent saved = clientConsentRepository.findByClientId(testClient.getId()).orElseThrow();
                assertThat(saved.isSmsOptIn()).isFalse();
        }

        @Test
        void testPublicUnsubscribeWithValidToken() throws Exception {
                // Given: Client has consent with unsubscribe token
                ClientConsent consent = ClientConsent.builder()
                                .clientId(testClient.getId())
                                .smsOptIn(true)
                                .smsUnsubToken(UUID.randomUUID().toString())
                                .build();
                clientConsentRepository.save(consent);

                String token = consent.getSmsUnsubToken();

                // When: Public unsubscribe request (no auth required)
                mockMvc.perform(post("/api/consent/unsubscribe/" + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Successfully unsubscribed from SMS notifications"));

                // Then: Client is unsubscribed
                ClientConsent updated = clientConsentRepository.findByClientId(testClient.getId()).orElseThrow();
                assertThat(updated.isSmsOptIn()).isFalse();
        }

        @Test
        void testPublicUnsubscribeWithInvalidToken() throws Exception {
                // When: Public unsubscribe with invalid token
                String invalidToken = UUID.randomUUID().toString();

                mockMvc.perform(post("/api/consent/unsubscribe/" + invalidToken))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid or expired unsubscribe token"));
        }

        @Test
        void testUnauthenticatedAccessDeniedForProtectedEndpoints() throws Exception {
                // When: Unauthenticated requests to protected endpoints
                mockMvc.perform(get("/api/client/me/consent"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(put("/api/client/me/consent")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testConsentTokenPersistsAcrossUpdates() throws Exception {
                // Given: Client gets initial consent (creates it)
                MvcResult initialResult = mockMvc.perform(get("/api/client/me/consent")
                                .header("Authorization", "Bearer " + clientToken))
                                .andExpect(status().isOk())
                                .andReturn();

                String initialResponse = initialResult.getResponse().getContentAsString();
                String initialToken = objectMapper.readTree(initialResponse)
                                .get("data").get("smsUnsubToken").asText();

                // When: Client updates consent
                ClientConsentUpdateRequest request = ClientConsentUpdateRequest.builder()
                                .smsOptIn(false)
                                .build();

                MvcResult updateResult = mockMvc.perform(put("/api/client/me/consent")
                                .header("Authorization", "Bearer " + clientToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String updateResponse = updateResult.getResponse().getContentAsString();
                String updatedToken = objectMapper.readTree(updateResponse)
                                .get("data").get("smsUnsubToken").asText();

                // Then: Token remains the same
                assertThat(updatedToken).isEqualTo(initialToken);
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

                // /api/auth/login returns ApiResponse<LoginResponse>
                JsonNode root = objectMapper.readTree(responseBody);
                String token = root.path("data").path("accessToken").asText(null);

                if (token == null || token.isBlank()) {
                        throw new IllegalStateException("Login OK but accessToken missing. Response: " + responseBody);
                }
                return token;
        }

}

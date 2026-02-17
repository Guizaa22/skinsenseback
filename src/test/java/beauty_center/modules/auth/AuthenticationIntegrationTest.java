package beauty_center.modules.auth;

import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.auth.dto.LoginResponse;
import beauty_center.modules.auth.dto.UserPrincipalDto;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JWT authentication endpoints.
 * Tests login flow, token validation, role-based access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

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
    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create test users
        createTestUser("admin@test.com", "Admin@123", Role.ADMIN);
        createTestUser("employee@test.com", "Employee@123", Role.EMPLOYEE);
        createTestUser("client@test.com", "Client@123", Role.CLIENT);

        // Obtain tokens
        adminToken = loginAndGetToken("admin@test.com", "Admin@123");
        employeeToken = loginAndGetToken("employee@test.com", "Employee@123");
        clientToken = loginAndGetToken("client@test.com", "Client@123");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email("admin@test.com")
            .password("Admin@123")
            .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("accessToken");
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLoginFailureInvalidPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email("admin@test.com")
            .password("WrongPassword123")
            .build();

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login for non-existent user")
    void testLoginFailureUserNotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .email("nonexistent@test.com")
            .password("Password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get current user details with valid token")
    void testGetCurrentUserSuccess() throws Exception {
        mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin@test.com"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should reject request without authentication token")
    void testGetCurrentUserNoToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with invalid token")
    void testGetCurrentUserInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() throws Exception {
        // First login to get refresh token
        LoginRequest loginRequest = LoginRequest.builder()
            .email("admin@test.com")
            .password("Admin@123")
            .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse)
            .get("data").get("refreshToken").asText();

        // Now refresh
        mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("Should allow admin to access admin-only endpoint")
    void testAdminRoleAccess() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
            .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny employee access to admin-only endpoint")
    void testEmployeeRoleDeniedAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
            .header("Authorization", "Bearer " + employeeToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny client access to admin-only endpoint")
    void testClientRoleDeniedAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
            .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isForbidden());
    }

    // ===== Helper Methods =====

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

}

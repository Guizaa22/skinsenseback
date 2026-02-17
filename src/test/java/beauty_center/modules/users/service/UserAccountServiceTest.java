package beauty_center.modules.users.service;

import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserAccountService Tests")
class UserAccountServiceTest {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create user with all required fields")
    void testCreateUserSuccess() {
        UserAccount user = UserAccount.builder()
            .fullName("Test User")
            .email("test@example.com")
            .phone("+216-123-4567")
            .build();

        UserAccount created = userAccountService.createUser(user, "SecurePassword123");

        assertNotNull(created.getId());
        assertEquals("Test User", created.getFullName());
        assertEquals("test@example.com", created.getEmail());
        assertEquals(Role.CLIENT, created.getRole());
        assertTrue(created.isActive());
        assertTrue(passwordEncoder.matches("SecurePassword123", created.getPasswordHash()));
    }

    @Test
    @DisplayName("Should set default CLIENT role if not specified")
    void testDefaultRoleAssignment() {
        UserAccount user = UserAccount.builder()
            .fullName("Client User")
            .email("client@example.com")
            .build();

        UserAccount created = userAccountService.createUser(user, "password123");
        assertEquals(Role.CLIENT, created.getRole());
    }

    @Test
    @DisplayName("Should reject user creation with missing full name")
    void testCreateUserMissingFullName() {
        UserAccount user = UserAccount.builder()
            .fullName("")
            .email("test@example.com")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> userAccountService.createUser(user, "password123"),
            "Full name is required");
    }

    @Test
    @DisplayName("Should reject user creation with missing email")
    void testCreateUserMissingEmail() {
        UserAccount user = UserAccount.builder()
            .fullName("Test User")
            .email("")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> userAccountService.createUser(user, "password123"),
            "Email is required");
    }

    @Test
    @DisplayName("Should reject user creation with missing password")
    void testCreateUserMissingPassword() {
        UserAccount user = UserAccount.builder()
            .fullName("Test User")
            .email("test@example.com")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> userAccountService.createUser(user, ""),
            "Password is required");
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void testCreateUserDuplicateEmail() {
        UserAccount user1 = UserAccount.builder()
            .fullName("User 1")
            .email("duplicate@example.com")
            .build();
        userAccountService.createUser(user1, "password123");

        UserAccount user2 = UserAccount.builder()
            .fullName("User 2")
            .email("duplicate@example.com")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> userAccountService.createUser(user2, "password123"),
            "Email already exists");
    }

    @Test
    @DisplayName("Should update user excluding email to another existing email")
    void testUpdateUserEmailUniqueness() {
        UserAccount user1 = UserAccount.builder()
            .fullName("User 1")
            .email("user1@example.com")
            .build();
        UserAccount created1 = userAccountService.createUser(user1, "password123");

        UserAccount user2 = UserAccount.builder()
            .fullName("User 2")
            .email("user2@example.com")
            .build();
        UserAccount created2 = userAccountService.createUser(user2, "password123");

        UserAccount updates = UserAccount.builder()
            .fullName("User 2 Updated")
            .email("user1@example.com")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> userAccountService.updateUser(created2.getId(), updates),
            "Email already exists");
    }
}


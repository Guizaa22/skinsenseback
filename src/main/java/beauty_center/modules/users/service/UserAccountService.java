package beauty_center.modules.users.service;

import beauty_center.modules.users.entity.AuthProvider;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User account service with business logic for user management.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<UserAccount> getUserById(UUID id) {
        return userAccountRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> getUserByEmail(String email) {
        return userAccountRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UserAccount> getUsersByRole(Role role) {
        return userAccountRepository.findByRole(role);
    }

    /**
     * Admin creates a new user (employee or another admin).
     */
    public UserAccount createUser(String fullName, String email, String phone,
                                   String plainPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .fullName(fullName.trim())
                .email(normalizedEmail)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(plainPassword))
                .active(true)
                .role(role)
                .emailVerified(false)
                .provider(AuthProvider.LOCAL)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        log.info("Admin created user: {} with role {}", normalizedEmail, role);
        return saved;
    }

    /**
     * Update user profile (name and phone only).
     */
    public UserAccount updateUser(UUID id, String fullName, String phone) {
        UserAccount existing = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (fullName != null && !fullName.isBlank()) {
            existing.setFullName(fullName.trim());
        }
        if (phone != null) {
            existing.setPhone(phone);
        }

        UserAccount saved = userAccountRepository.save(existing);
        log.info("User profile updated: {}", id);
        return saved;
    }

    public void deactivateUser(UUID id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(false);
        userAccountRepository.save(user);
        log.info("User deactivated: {}", id);
    }

    public void activateUser(UUID id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(true);
        userAccountRepository.save(user);
        log.info("User activated: {}", id);
    }
}

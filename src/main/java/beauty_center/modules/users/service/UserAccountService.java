package beauty_center.modules.users.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.users.dto.UserUpdateRequest;
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
        if (email == null) return Optional.empty();
        return userAccountRepository.findByEmail(normalizeEmail(email));
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
     * Create new user account (typically self-registration).
     */
    public UserAccount createUser(UserAccount user, String plainPassword) {
        if (user == null) throw new IllegalArgumentException("User is required");

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String normalizedEmail = normalizeEmail(user.getEmail());

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Ensure ID exists if your DB doesn't auto-generate UUIDs
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }

        user.setFullName(user.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));

        // Enforce server-side defaults
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }

        // MVP requirement: new users are active
        user.setActive(true);

        // If your entity supports these fields (Code2 suggests it does)
        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.LOCAL);
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(false);
        }

        UserAccount saved = userAccountRepository.save(user);
        log.info("User created: {}", normalizedEmail);
        return saved;
    }

    /**
     * Admin creates a new user (employee or another admin).
     * Kept to preserve Code2 call sites.
     */
    public UserAccount createUser(String fullName, String email, String phone,
                                  String plainPassword, Role role) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }

        String normalizedEmail = normalizeEmail(email);

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setActive(true);
        user.setRole(role);

        // Code2 fields
        user.setEmailVerified(false);
        user.setProvider(AuthProvider.LOCAL);

        UserAccount saved = userAccountRepository.save(user);
        log.info("Admin created user: {} with role {}", normalizedEmail, role);
        return saved;
    }

    /**
     * Update existing user (admin-style: name/email/phone).
     */
    public UserAccount updateUser(UUID id, UserAccount updates) {
        UserAccount existing = userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        if (updates == null) return existing;

        if (updates.getFullName() != null && !updates.getFullName().trim().isEmpty()) {
            existing.setFullName(updates.getFullName().trim());
        }

        if (updates.getEmail() != null && !updates.getEmail().trim().isEmpty()) {
            String newEmail = normalizeEmail(updates.getEmail());

            if (!existing.getEmail().equalsIgnoreCase(newEmail)
                    && userAccountRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }

            existing.setEmail(newEmail);
        }

        if (updates.getPhone() != null) {
            existing.setPhone(updates.getPhone());
        }

        UserAccount saved = userAccountRepository.save(existing);
        log.info("User updated: {}", id);
        return saved;
    }

    /**
     * Update user profile (name and phone only) - kept to preserve Code2 call sites.
     */
    public UserAccount updateUser(UUID id, String fullName, String phone) {
        UserAccount updates = new UserAccount();
        updates.setFullName(fullName);
        updates.setPhone(phone);
        return updateUser(id, updates);
    }

    /**
     * Update user from DTO (name and phone).
     */
    public UserAccount updateUser(UUID id, UserUpdateRequest request) {
        if (request == null) return userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return updateUser(id, request.getFullName(), request.getPhone());
    }

    /**
     * Change password for the given user. Verifies current password first.
     */
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    public void deactivateUser(UUID id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        user.setActive(false);
        userAccountRepository.save(user);
        log.info("User deactivated: {}", id);
    }

    public void activateUser(UUID id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        user.setActive(true);
        userAccountRepository.save(user);
        log.info("User activated: {}", id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
package beauty_center.modules.users.service;

import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * User account service with business logic for user management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user by ID
     */
    public Optional<UserAccount> getUserById(UUID id) {
        return userAccountRepository.findById(id);
    }

    /**
     * Get user by email
     */
    public Optional<UserAccount> getUserByEmail(String email) {
        return userAccountRepository.findByEmail(email);
    }

    /**
     * Create new user account
     */
    public UserAccount createUser(UserAccount user, String plainPassword) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userAccountRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setPasswordHash(passwordEncoder.encode(plainPassword));

        // Enforce server-side defaults
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }

        // Ensure newly created users are always active (MVP requirement)
        user.setActive(true);

        return userAccountRepository.save(user);
    }

    /**
     * Update existing user
     */
    public UserAccount updateUser(UUID id, UserAccount updates) {
        UserAccount existing = userAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updates.getFullName() != null && !updates.getFullName().trim().isEmpty()) {
            existing.setFullName(updates.getFullName());
        }

        if (updates.getEmail() != null && !updates.getEmail().trim().isEmpty()) {
            if (!existing.getEmail().equals(updates.getEmail()) &&
                userAccountRepository.existsByEmail(updates.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            existing.setEmail(updates.getEmail());
        }

        if (updates.getPhone() != null) {
            existing.setPhone(updates.getPhone());
        }

        return userAccountRepository.save(existing);
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(UUID id) {
        userAccountRepository.findById(id).ifPresent(user -> {
            user.setActive(false);
            userAccountRepository.save(user);
        });
    }

    /**
     * Activate user account
     */
    public void activateUser(UUID id) {
        userAccountRepository.findById(id).ifPresent(user -> {
            user.setActive(true);
            userAccountRepository.save(user);
        });
    }

}

package beauty_center.modules.users.service;

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
        if (userAccountRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password before storing
        user.setPasswordHash(passwordEncoder.encode(plainPassword));

        // TODO: Set default role if not specified
        // TODO: Validate user has all required fields

        return userAccountRepository.save(user);
    }

    /**
     * Update existing user
     */
    public UserAccount updateUser(UUID id, UserAccount updates) {
        UserAccount existing = userAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // TODO: Validate email uniqueness (excluding current user)
        existing.setFullName(updates.getFullName());
        existing.setPhone(updates.getPhone());

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

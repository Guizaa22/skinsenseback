package beauty_center.modules.users.repository;

import beauty_center.modules.users.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserAccount persistence operations
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    /**
     * Find user by email address
     */
    Optional<UserAccount> findByEmail(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

}

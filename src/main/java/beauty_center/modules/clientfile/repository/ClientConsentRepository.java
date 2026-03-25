package beauty_center.modules.clientfile.repository;

import beauty_center.modules.clientfile.entity.ClientConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ClientConsent persistence operations.
 * Manages client notification preferences and SMS opt-in settings.
 */
@Repository
public interface ClientConsentRepository extends JpaRepository<ClientConsent, UUID> {

    /**
     * Find client consent by client ID.
     * Each client has exactly one consent record.
     *
     * @param clientId UUID of the client
     * @return Optional containing the client consent if found
     */
    Optional<ClientConsent> findByClientId(UUID clientId);

    /**
     * Find client consent by unsubscribe token.
     * Used for SMS unsubscribe functionality.
     *
     * @param smsUnsubToken Unsubscribe token
     * @return Optional containing the client consent if found
     */
    Optional<ClientConsent> findBySmsUnsubToken(String smsUnsubToken);

    /**
     * Check if client consent exists for a client.
     *
     * @param clientId UUID of the client
     * @return true if consent exists, false otherwise
     */
    boolean existsByClientId(UUID clientId);

    /**
     * Delete client consent by client ID.
     *
     * @param clientId UUID of the client
     */
    void deleteByClientId(UUID clientId);
}


package beauty_center.modules.clientfile.repository;

import beauty_center.modules.clientfile.entity.ClientFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ClientFile persistence operations.
 * Manages client medical and personal information files.
 */
@Repository
public interface ClientFileRepository extends JpaRepository<ClientFile, UUID> {

    /**
     * Find client file by client ID.
     * Each client has at most one file.
     *
     * @param clientId UUID of the client
     * @return Optional containing the client file if found
     */
    Optional<ClientFile> findByClientId(UUID clientId);

    /**
     * Check if client file exists for a client.
     *
     * @param clientId UUID of the client
     * @return true if file exists, false otherwise
     */
    boolean existsByClientId(UUID clientId);

    /**
     * Delete client file by client ID.
     *
     * @param clientId UUID of the client
     */
    void deleteByClientId(UUID clientId);
}


package beauty_center.modules.services.repository;

import beauty_center.modules.services.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Specialty persistence
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    /**
     * Find specialty by name
     */
    Optional<Specialty> findByName(String name);

    /**
     * Check if specialty exists by name
     */
    boolean existsByName(String name);

}


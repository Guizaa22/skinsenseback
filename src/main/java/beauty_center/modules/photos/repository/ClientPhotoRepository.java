package beauty_center.modules.photos.repository;

import beauty_center.modules.photos.entity.ClientPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClientPhotoRepository extends JpaRepository<ClientPhoto, UUID> {
    List<ClientPhoto> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<ClientPhoto> findByClientIdAndLabelOrderByCreatedAtDesc(UUID clientId, String label);
}

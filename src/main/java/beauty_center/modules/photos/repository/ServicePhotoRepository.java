package beauty_center.modules.photos.repository;

import beauty_center.modules.photos.entity.ServicePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ServicePhotoRepository extends JpaRepository<ServicePhoto, UUID> {
    List<ServicePhoto> findByServiceIdOrderByCreatedAtDesc(UUID serviceId);
}

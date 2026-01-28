package beauty_center.modules.services.service;

import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Beauty service service managing available services.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BeautyServiceService {

    private final BeautyServiceRepository beautyServiceRepository;

    /**
     * Get all active services
     */
    public List<BeautyService> getAllActiveServices() {
        return beautyServiceRepository.findByIsActiveTrue();
    }

    /**
     * Get service by ID
     */
    public Optional<BeautyService> getServiceById(UUID id) {
        return beautyServiceRepository.findById(id);
    }

    /**
     * Create new service
     */
    public BeautyService createService(BeautyService service) {
        // TODO: Validate price is positive
        // TODO: Validate duration is reasonable
        return beautyServiceRepository.save(service);
    }

    /**
     * Update service
     */
    public BeautyService updateService(UUID id, BeautyService updates) {
        return beautyServiceRepository.save(updates);
    }

    /**
     * Deactivate service
     */
    public void deactivateService(UUID id) {
        beautyServiceRepository.findById(id).ifPresent(service -> {
            service.setActive(false);
            beautyServiceRepository.save(service);
        });
    }

}

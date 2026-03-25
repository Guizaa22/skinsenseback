package beauty_center.modules.services.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.services.entity.Specialty;
import beauty_center.modules.services.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Specialty service managing specialty operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    /**
     * Get all specialties
     */
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    /**
     * Get specialty by ID
     */
    public Optional<Specialty> getSpecialtyById(UUID id) {
        return specialtyRepository.findById(id);
    }

    /**
     * Create new specialty
     */
    public Specialty createSpecialty(Specialty specialty) {
        if (specialtyRepository.existsByName(specialty.getName())) {
            throw new IllegalArgumentException("Specialty with this name already exists");
        }
        return specialtyRepository.save(specialty);
    }

    /**
     * Update specialty
     */
    public Specialty updateSpecialty(UUID id, Specialty updates) {
        Specialty existing = specialtyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Specialty", id));

        // Check if name is being changed and if it conflicts with another specialty
        if (!existing.getName().equals(updates.getName()) && 
            specialtyRepository.existsByName(updates.getName())) {
            throw new IllegalArgumentException("Specialty with this name already exists");
        }

        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());

        return specialtyRepository.save(existing);
    }

    /**
     * Delete specialty
     */
    public void deleteSpecialty(UUID id) {
        if (!specialtyRepository.existsById(id)) {
            throw new EntityNotFoundException("Specialty", id);
        }
        specialtyRepository.deleteById(id);
    }

}


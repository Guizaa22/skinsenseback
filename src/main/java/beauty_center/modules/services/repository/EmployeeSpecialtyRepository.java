package beauty_center.modules.services.repository;

import beauty_center.modules.services.entity.EmployeeSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for EmployeeSpecialty persistence
 */
@Repository
public interface EmployeeSpecialtyRepository extends JpaRepository<EmployeeSpecialty, UUID> {

    /**
     * Find all specialties for an employee
     */
    List<EmployeeSpecialty> findByEmployeeId(UUID employeeId);

    /**
     * Find all employees with a specific specialty
     */
    List<EmployeeSpecialty> findBySpecialtyId(UUID specialtyId);

    /**
     * Find specific employee-specialty relationship
     */
    Optional<EmployeeSpecialty> findByEmployeeIdAndSpecialtyId(UUID employeeId, UUID specialtyId);

    /**
     * Check if employee has a specific specialty
     */
    boolean existsByEmployeeIdAndSpecialtyId(UUID employeeId, UUID specialtyId);

    /**
     * Delete employee-specialty relationship
     */
    void deleteByEmployeeIdAndSpecialtyId(UUID employeeId, UUID specialtyId);

}


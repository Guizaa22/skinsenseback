package beauty_center.modules.services.repository;

import beauty_center.modules.services.entity.BeautyServiceEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for BeautyServiceEmployee persistence
 */
@Repository
public interface BeautyServiceEmployeeRepository extends JpaRepository<BeautyServiceEmployee, UUID> {

    /**
     * Find all employees allowed for a service
     */
    List<BeautyServiceEmployee> findByBeautyServiceId(UUID beautyServiceId);

    /**
     * Find all services an employee can perform
     */
    List<BeautyServiceEmployee> findByEmployeeId(UUID employeeId);

    /**
     * Find specific service-employee relationship
     */
    Optional<BeautyServiceEmployee> findByBeautyServiceIdAndEmployeeId(UUID beautyServiceId, UUID employeeId);

    /**
     * Check if employee is allowed for a service
     */
    boolean existsByBeautyServiceIdAndEmployeeId(UUID beautyServiceId, UUID employeeId);

    /**
     * Delete service-employee relationship
     */
    void deleteByBeautyServiceIdAndEmployeeId(UUID beautyServiceId, UUID employeeId);

    /**
     * Delete all employees for a service
     */
    void deleteByBeautyServiceId(UUID beautyServiceId);

}


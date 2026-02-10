package beauty_center.modules.services.service;

import beauty_center.modules.services.entity.EmployeeSpecialty;
import beauty_center.modules.services.entity.Specialty;
import beauty_center.modules.services.repository.EmployeeSpecialtyRepository;
import beauty_center.modules.services.repository.SpecialtyRepository;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing employee-specialty relationships.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeSpecialtyService {

    private final EmployeeSpecialtyRepository employeeSpecialtyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * Assign specialty to employee
     */
    public EmployeeSpecialty assignSpecialtyToEmployee(UUID employeeId, UUID specialtyId) {
        // Validate employee exists
        if (!userAccountRepository.existsById(employeeId)) {
            throw new IllegalArgumentException("Employee not found");
        }

        // Validate specialty exists
        if (!specialtyRepository.existsById(specialtyId)) {
            throw new IllegalArgumentException("Specialty not found");
        }

        // Check if already assigned
        if (employeeSpecialtyRepository.existsByEmployeeIdAndSpecialtyId(employeeId, specialtyId)) {
            throw new IllegalArgumentException("Employee already has this specialty");
        }

        EmployeeSpecialty employeeSpecialty = EmployeeSpecialty.builder()
            .employeeId(employeeId)
            .specialtyId(specialtyId)
            .build();

        return employeeSpecialtyRepository.save(employeeSpecialty);
    }

    /**
     * Remove specialty from employee
     */
    public void removeSpecialtyFromEmployee(UUID employeeId, UUID specialtyId) {
        if (!employeeSpecialtyRepository.existsByEmployeeIdAndSpecialtyId(employeeId, specialtyId)) {
            throw new IllegalArgumentException("Employee does not have this specialty");
        }

        employeeSpecialtyRepository.deleteByEmployeeIdAndSpecialtyId(employeeId, specialtyId);
    }

    /**
     * Get all specialties for an employee
     */
    public List<Specialty> getEmployeeSpecialties(UUID employeeId) {
        List<UUID> specialtyIds = employeeSpecialtyRepository.findByEmployeeId(employeeId)
            .stream()
            .map(EmployeeSpecialty::getSpecialtyId)
            .collect(Collectors.toList());

        return specialtyRepository.findAllById(specialtyIds);
    }

    /**
     * Get all employees with a specific specialty
     */
    public List<UUID> getEmployeesWithSpecialty(UUID specialtyId) {
        return employeeSpecialtyRepository.findBySpecialtyId(specialtyId)
            .stream()
            .map(EmployeeSpecialty::getEmployeeId)
            .collect(Collectors.toList());
    }

}


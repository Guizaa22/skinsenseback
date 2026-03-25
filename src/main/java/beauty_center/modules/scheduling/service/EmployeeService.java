package beauty_center.modules.scheduling.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.scheduling.dto.EmployeeCreateRequest;
import beauty_center.modules.scheduling.dto.EmployeeResponse;
import beauty_center.modules.scheduling.dto.EmployeeUpdateRequest;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Employee service managing employee accounts and operations.
 * Handles CRUD operations for employees with EMPLOYEE role.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create new employee account
     */
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        log.info("Creating new employee: {}", request.getEmail());

        // Validate email uniqueness
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Create user account with EMPLOYEE role
        UserAccount employee = UserAccount.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(Role.EMPLOYEE)
            .active(true)
            .build();

        UserAccount saved = userAccountRepository.save(employee);
        log.info("Employee created successfully: {} (ID: {})", saved.getEmail(), saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Get employee by ID
     */
    public Optional<EmployeeResponse> getEmployeeById(UUID id) {
        return userAccountRepository.findById(id)
            .filter(user -> user.getRole() == Role.EMPLOYEE)
            .map(this::mapToResponse);
    }

    /**
     * Get all employees with optional filters
     */
    public List<EmployeeResponse> getAllEmployees(Boolean isActive) {
        List<UserAccount> employees = userAccountRepository.findAll().stream()
            .filter(user -> user.getRole() == Role.EMPLOYEE)
            .filter(user -> isActive == null || user.isActive() == isActive)
            .collect(Collectors.toList());

        return employees.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update employee information
     */
    public EmployeeResponse updateEmployee(UUID id, EmployeeUpdateRequest request) {
        log.info("Updating employee: {}", id);

        UserAccount employee = userAccountRepository.findById(id)
            .filter(user -> user.getRole() == Role.EMPLOYEE)
            .orElseThrow(() -> new EntityNotFoundException("Employee", id));

        // Update fields if provided
        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            // Validate email uniqueness (excluding current employee)
            if (!employee.getEmail().equals(request.getEmail()) &&
                userAccountRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getIsActive() != null) {
            employee.setActive(request.getIsActive());
        }

        UserAccount updated = userAccountRepository.save(employee);
        log.info("Employee updated successfully: {}", id);

        return mapToResponse(updated);
    }

    /**
     * Activate employee account
     */
    public void activateEmployee(UUID id) {
        log.info("Activating employee: {}", id);
        UserAccount employee = userAccountRepository.findById(id)
            .filter(user -> user.getRole() == Role.EMPLOYEE)
            .orElseThrow(() -> new EntityNotFoundException("Employee", id));

        employee.setActive(true);
        userAccountRepository.save(employee);
        log.info("Employee activated: {}", id);
    }

    /**
     * Deactivate employee account
     */
    public void deactivateEmployee(UUID id) {
        log.info("Deactivating employee: {}", id);
        UserAccount employee = userAccountRepository.findById(id)
            .filter(user -> user.getRole() == Role.EMPLOYEE)
            .orElseThrow(() -> new EntityNotFoundException("Employee", id));

        employee.setActive(false);
        userAccountRepository.save(employee);
        log.info("Employee deactivated: {}", id);
    }

    /**
     * Map UserAccount entity to EmployeeResponse DTO
     */
    private EmployeeResponse mapToResponse(UserAccount user) {
        return EmployeeResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .isActive(user.isActive())
            .role(user.getRole().name())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

}


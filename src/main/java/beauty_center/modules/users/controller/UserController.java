package beauty_center.modules.users.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.users.dto.ChangePasswordRequest;
import beauty_center.modules.users.dto.UserCreateRequest;
import beauty_center.modules.users.dto.UserResponse;
import beauty_center.modules.users.dto.UserUpdateRequest;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.service.UserAccountService;
import beauty_center.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User management controller.
 * - ADMIN: Full CRUD on all users, list by role, activate/deactivate
 * - EMPLOYEE/CLIENT: View and update own profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserAccountService userAccountService;
    private final CurrentUser currentUser;

    /**
     * Get current authenticated user profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        log.info("User profile requested by: {}", currentUser.getUsername());

        UUID userId = currentUser.getUserId();
        UserAccount user = userAccountService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        return ResponseEntity.ok(
                ApiResponse.ok(UserResponse.fromEntity(user), "Profile retrieved successfully")
        );
    }

    /**
     * Update own profile (name and phone).
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {

        UUID userId = currentUser.getUserId();
        UserAccount updated = userAccountService.updateUser(userId, request);
        return ResponseEntity.ok(
                ApiResponse.ok(UserResponse.fromEntity(updated), "Profile updated successfully")
        );
    }

    /**
     * Change current user's password.
     * Requires current password for verification.
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request");
        UUID userId = currentUser.getUserId();
        userAccountService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(null, "Password changed successfully"));
    }

    /**
     * Get user by ID.
     * ADMIN can view any user; others can only view their own.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("User details requested for ID: {} by {}", id, currentUser.getUsername());

        UserAccount user = userAccountService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        if (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("EMPLOYEE") && !currentUser.getUserId().equals(id)) {
            throw new AccessDeniedException("You can only view your own profile");
        }

        return ResponseEntity.ok(
                ApiResponse.ok(UserResponse.fromEntity(user), "User retrieved successfully")
        );
    }

    /**
     * List all users (ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userAccountService.getAllUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(users, "Users retrieved successfully"));
    }

    /**
     * List employees (ADMIN only).
     */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listEmployees() {
        List<UserResponse> employees = userAccountService.getUsersByRole(Role.EMPLOYEE).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(employees, "Employees retrieved successfully"));
    }

    /**
     * List clients (ADMIN only).
     */
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listClients() {
        List<UserResponse> clients = userAccountService.getUsersByRole(Role.CLIENT).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(clients, "Clients retrieved successfully"));
    }

    /**
     * Create a new user (ADMIN only).
     * Supports creating ADMIN/EMPLOYEE/CLIENT.
     * If request.role is missing/null, default to CLIENT (matches Code1 behavior).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        log.info("Create user request from ADMIN: {}", currentUser.getUsername());

        Role role = parseRoleOrDefault(request.getRole());

        UserAccount created = userAccountService.createUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                role
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(UserResponse.fromEntity(created), "User created successfully"));
    }

    /**
     * Update any user (ADMIN only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {

        UserAccount updated = userAccountService.updateUser(
                id, request.getFullName(), request.getPhone());

        return ResponseEntity.ok(
                ApiResponse.ok(UserResponse.fromEntity(updated), "User updated successfully"));
    }

    /**
     * Deactivate user (ADMIN only).
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userAccountService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "User deactivated successfully"));
    }

    /**
     * Activate user (ADMIN only).
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userAccountService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "User activated successfully"));
    }

    private Role parseRoleOrDefault(String roleRaw) {
        if (roleRaw == null || roleRaw.isBlank()) {
            return Role.CLIENT; // keeps Code1 default behavior
        }
        try {
            return Role.valueOf(roleRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // keep consistent with your ApiResponse signature used elsewhere (message + statusCode)
            throw new IllegalArgumentException("Invalid role. Must be ADMIN, EMPLOYEE, or CLIENT");
        }
    }
}
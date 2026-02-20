package beauty_center.modules.users.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.users.dto.UserCreateRequest;
import beauty_center.modules.users.dto.UserResponse;
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

import java.util.UUID;

/**
 * User management controller with role-based access control.
 *
 * - ADMIN: Can create users, activate/deactivate accounts
 * - EMPLOYEE: Can view own profile
 * - CLIENT: Can view own profile
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

        String email = currentUser.getUsername();
        UserAccount user = userAccountService.getUserByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User", email));

        return ResponseEntity.ok(
            ApiResponse.ok(UserResponse.fromEntity(user), "User profile retrieved successfully")
        );
    }

    /**
     * Get user by ID (ADMIN can view any, others can view their own).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("User details requested for ID: {}", id);

        UserAccount user = userAccountService.getUserById(id)
            .orElseThrow(() -> new EntityNotFoundException("User", id));

        if (!currentUser.hasRole("ADMIN") && !user.getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("Access denied: You can only view your own profile");
        }

        return ResponseEntity.ok(
            ApiResponse.ok(UserResponse.fromEntity(user), "User retrieved successfully")
        );
    }

    /**
     * Create new user account (ADMIN only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Create user request from ADMIN: {}", currentUser.getUsername());

        try {
            UserAccount newUser = UserAccount.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.CLIENT)
                .build();

            UserAccount created = userAccountService.createUser(newUser, request.getPassword());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(UserResponse.fromEntity(created), "User created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Deactivate user account (ADMIN only).
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        log.warn("Deactivate user request from ADMIN for user: {} by {}", id, currentUser.getUsername());

        try {
            userAccountService.deactivateUser(id);
            return ResponseEntity.ok(
                ApiResponse.ok(null, "User deactivated successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Activate user account (ADMIN only).
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        log.info("Activate user request from ADMIN for user: {} by {}", id, currentUser.getUsername());

        try {
            userAccountService.activateUser(id);
            return ResponseEntity.ok(
                ApiResponse.ok(null, "User activated successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
    }


}

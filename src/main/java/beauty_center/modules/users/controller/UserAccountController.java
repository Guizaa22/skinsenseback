package beauty_center.modules.users.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.users.dto.UserCreateRequest;
import beauty_center.modules.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * User account REST controller for user management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    // TODO: Inject UserAccountService

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        // TODO: Get user with proper authorization
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        // TODO: Get user by email address
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        // TODO: Create user with password hashing
        // TODO: Set default role based on endpoint context
        return ResponseEntity.ok(ApiResponse.ok(null, "User created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserCreateRequest request) {
        // TODO: Update user (authorization check)
        return ResponseEntity.ok(ApiResponse.ok(null, "User updated"));
    }

}

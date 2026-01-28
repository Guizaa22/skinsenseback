package beauty_center.modules.auth;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.auth.dto.LoginResponse;
import beauty_center.modules.auth.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication controller handling login, logout, and token refresh.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Login endpoint - authenticate user and return JWT tokens
     *
     * @param request Login credentials (email + password)
     * @return AccessToken and RefreshToken
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        // TODO: Authenticate user with email and password
        // TODO: Validate credentials against database
        // TODO: Generate access token + refresh token
        // TODO: Return response with tokens

        LoginResponse response = LoginResponse.builder()
            .accessToken("TODO_ACCESS_TOKEN")
            .refreshToken("TODO_REFRESH_TOKEN")
            .expiresIn(3600)
            .build();

        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    /**
     * Refresh token endpoint - get new access token using refresh token
     *
     * @param request Refresh token
     * @return New AccessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        // TODO: Validate refresh token
        // TODO: Extract user from refresh token
        // TODO: Generate new access token
        // TODO: Return new token

        LoginResponse response = LoginResponse.builder()
            .accessToken("TODO_NEW_ACCESS_TOKEN")
            .refreshToken(request.getRefreshToken())
            .expiresIn(3600)
            .build();

        return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed"));
    }

    /**
     * Logout endpoint - invalidate tokens
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // TODO: Invalidate refresh token in database if needed
        // TODO: Clear security context

        return ResponseEntity.ok(ApiResponse.ok(null, "Logout successful"));
    }

}

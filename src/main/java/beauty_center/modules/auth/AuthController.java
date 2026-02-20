package beauty_center.modules.auth;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.auth.dto.LoginResponse;
import beauty_center.modules.auth.dto.RefreshRequest;
import beauty_center.modules.auth.dto.RegisterRequest;
import beauty_center.modules.auth.dto.UserPrincipalDto;
import beauty_center.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication controller handling login, logout, and token refresh.
 * All endpoints return standardized ApiResponse wrapper.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new client user.
     * Public endpoint (no authentication required).
     * Creates user with CLIENT role, empty ClientFile, and ClientConsent.
     * Returns JWT tokens for immediate sign-in.
     *
     * @param request Registration data (fullName, email, phone, password)
     * @return LoginResponse with accessToken and refreshToken (201 Created)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());

        try {
            LoginResponse response = authService.register(request);
            log.info("User registered successfully: {}", request.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Registration successful"));
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
        } catch (Exception e) {
            log.error("Registration error for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Login endpoint - authenticate user and return JWT tokens.
     * Public endpoint (no authentication required).
     *
     * @param request Login credentials (email + password)
     * @return LoginResponse with accessToken, refreshToken, and expiresIn
     * @throws AuthenticationException if credentials invalid
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());

        try {
            LoginResponse response = authService.login(request);
            log.info("User logged in successfully: {}", request.getEmail());

            return ResponseEntity.ok(
                ApiResponse.ok(response, "Login successful")
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password", HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("Login error for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Login failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get current authenticated user details.
     * Requires authentication (any authenticated user).
     *
     * @return UserPrincipalDto with current user information
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPrincipalDto>> getCurrentUser() {
        log.debug("Fetching current user details");

        try {
            UserPrincipalDto user = authService.getCurrentUser();
            return ResponseEntity.ok(
                ApiResponse.ok(user, "User details retrieved successfully")
            );
        } catch (IllegalArgumentException e) {
            log.warn("Could not retrieve current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Refresh token endpoint - get new access token using refresh token.
     * Public endpoint (no authentication required for refresh token).
     *
     * @param request Refresh token request
     * @return LoginResponse with new accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        log.debug("Refresh token request");

        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            log.debug("Token refreshed successfully");

            return ResponseEntity.ok(
                ApiResponse.ok(response, "Token refreshed successfully")
            );
        } catch (IllegalArgumentException e) {
            log.warn("Refresh token failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED.value()));
        }
    }

    /**
     * Logout endpoint - invalidate tokens.
     * Requires authentication (any authenticated user).
     *
     * @return Success message
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request from user");

        try {
            authService.logout();
            return ResponseEntity.ok(
                ApiResponse.ok(null, "Logout successful")
            );
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

}

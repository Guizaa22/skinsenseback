package beauty_center.modules.auth.service;

import beauty_center.modules.audit.entity.AuditEntry;
import beauty_center.modules.audit.repository.AuditRepository;
import beauty_center.modules.auth.dto.LoginRequest;
import beauty_center.modules.auth.dto.LoginResponse;
import beauty_center.modules.auth.dto.RegisterRequest;
import beauty_center.modules.auth.dto.UserPrincipalDto;
import beauty_center.modules.clientfile.entity.ClientConsent;
import beauty_center.modules.clientfile.entity.ClientFile;
import beauty_center.modules.clientfile.repository.ClientConsentRepository;
import beauty_center.modules.clientfile.repository.ClientFileRepository;
import beauty_center.modules.users.entity.AuthProvider;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import beauty_center.security.CurrentUser;
import beauty_center.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication service handling login/logout, registration, and token generation.
 * Uses Spring Security AuthenticationManager for credential validation.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientFileRepository clientFileRepository;
    private final ClientConsentRepository clientConsentRepository;
    private final AuditRepository auditRepository;
    private final CurrentUser currentUserHelper;

    /**
     * Register a new client user with email and password.
     * Creates the UserAccount, empty ClientFile, ClientConsent, and logs an audit entry.
     * Returns JWT tokens so the user is immediately signed in.
     *
     * @param request Registration request with fullName, email, phone, password
     * @return LoginResponse with access token and refresh token
     * @throws IllegalArgumentException if email is already taken
     */
    public LoginResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        log.info("Registration attempt for email: {}", normalizedEmail);

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration failed - email already exists: {}", normalizedEmail);
            throw new IllegalArgumentException("Email is already registered");
        }

        // 1. Create UserAccount with CLIENT role and LOCAL provider
        UserAccount user = UserAccount.builder()
                .id(UUID.randomUUID())
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .role(Role.CLIENT)
                .emailVerified(false)
                .provider(AuthProvider.LOCAL)
                .build();

        UserAccount savedUser = userAccountRepository.save(user);
        log.info("User registered successfully: {}", normalizedEmail);

        // 2. Create empty ClientFile
        ClientFile clientFile = ClientFile.builder()
                .id(UUID.randomUUID())
                .clientId(savedUser.getId())
                .build();
        clientFileRepository.save(clientFile);
        log.debug("Empty ClientFile created for user: {}", savedUser.getId());

        // 3. Create ClientConsent (smsOptIn = false by default)
        ClientConsent consent = ClientConsent.builder()
                .id(UUID.randomUUID())
                .clientId(savedUser.getId())
                .smsOptIn(false)
                .smsUnsubToken(UUID.randomUUID().toString())
                .build();
        clientConsentRepository.save(consent);
        log.debug("ClientConsent created for user: {}", savedUser.getId());

        // 4. Audit log (user is not authenticated yet, set actorId explicitly)
        AuditEntry auditEntry = AuditEntry.builder()
                .entityType("UserAccount")
                .entityId(savedUser.getId())
                .action("REGISTER")
                .actorId(savedUser.getId())
                .at(OffsetDateTime.now())
                .afterJson("{\"email\":\"" + normalizedEmail + "\",\"role\":\"CLIENT\",\"provider\":\"LOCAL\"}")
                .build();
        auditRepository.save(auditEntry);

        // 5. Generate JWT tokens for immediate sign-in
        List<String> roles = List.of("ROLE_" + savedUser.getRole().name());
        String accessToken = jwtService.generateToken(
                normalizedEmail,
                roles,
                60 * 60
        );
        String refreshToken = jwtService.generateRefreshToken(normalizedEmail, roles);

        log.info("Registration complete, tokens issued for: {}", normalizedEmail);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(60 * 60)
                .build();
    }

    /**
     * Authenticate user with email and password.
     * Generates access and refresh tokens on successful authentication.
     *
     * @param request Login request containing email and password
     * @return LoginResponse with access token, refresh token, and expiration
     * @throws org.springframework.security.core.AuthenticationException if credentials invalid
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            log.info("User authenticated successfully: {}", request.getEmail());

            // Extract roles from authentication
            List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(request.getEmail(), roles);

            log.debug("Tokens generated for user: {}", request.getEmail());

            return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(60 * 60) // 1 hour in seconds
                .build();

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Refresh access token using refresh token.
     * Validates refresh token and generates new access token with same roles.
     *
     * @param refreshToken Refresh token from login response
     * @return LoginResponse with new access token
     * @throws IllegalArgumentException if refresh token invalid or expired
     */
    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        if (!jwtService.isTokenValid(refreshToken)) {
            log.warn("Refresh token validation failed");
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        List<String> roles = jwtService.extractRoles(refreshToken);

        log.debug("Generating new access token for user: {}", username);

        String newAccessToken = jwtService.generateToken(
            username,
            roles,
            60 * 60 // 1 hour
        );

        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(60 * 60)
            .build();
    }

    /**
     * Get current authenticated user details.
     * Retrieves user information from database using SecurityContext.
     *
     * @return UserPrincipalDto with current user info
     * @throws IllegalArgumentException if user not found
     */
    public UserPrincipalDto getCurrentUser() {
        String email = currentUserHelper.getUsername();
        log.debug("Fetching current user details for: {}", email);

        if (email == null) {
            throw new IllegalArgumentException("No authenticated user found");
        }

        UserAccount user = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return UserPrincipalDto.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole())
            .active(user.isActive())
            .build();
    }

    /**
     * Logout user.
     * In stateless JWT architecture, client simply discards token.
     * This method exists for audit trail and future blacklist implementation.
     */
    public void logout() {
        String email = currentUserHelper.getUsername();
        log.info("User logged out: {}", email);
        // TODO: Implement token blacklist if needed in future phases
        SecurityContextHolder.clearContext();
    }

}

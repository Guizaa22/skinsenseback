package beauty_center.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT token generation and validation service.
 * Generates tokens with role claims in format ["ROLE_ADMIN", "ROLE_EMPLOYEE", "ROLE_CLIENT"].
 * Validates tokens and extracts claims including roles and username.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;
    private static final String ROLES_CLAIM = "roles";

    /**
     * Generate access token from authentication.
     * Includes username and roles as JWT claims.
     *
     * @param authentication Spring Security Authentication object containing username and authorities
     * @return JWT access token string
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();

        // Extract roles from authorities: ROLE_ADMIN -> add to list
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        log.debug("Generating access token for user: {} with roles: {}", username, roles);

        return generateToken(
            username,
            roles,
            jwtProperties.getExpirationMinutes() * 60
        );
    }

    /**
     * Generate refresh token with longer expiration.
     * Contains username and roles but typically used for token refresh only.
     *
     * @param username User email/username
     * @param roles User roles
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String username, List<String> roles) {
        log.debug("Generating refresh token for user: {}", username);

        return generateToken(
            username,
            roles,
            jwtProperties.getRefreshExpirationDays() * 24 * 60 * 60
        );
    }

    /**
     * Generate JWT token with custom claims (public utility method).
     * Used for refresh token flow to generate new access tokens.
     *
     * @param subject Username (email)
     * @param roles List of role strings (ROLE_ADMIN format)
     * @param expirationSeconds Token expiration in seconds
     * @return Signed JWT token
     */
    public String generateToken(String subject, List<String> roles, long expirationSeconds) {
        byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (expirationSeconds * 1000));

        return Jwts.builder()
            .setSubject(subject)
            .claim(ROLES_CLAIM, roles)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(Keys.hmacShaKeyFor(secretKey))
            .compact();
    }

    /**
     * Extract username from JWT token.
     *
     * @param token JWT token string
     * @return Username (email) from token subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract roles from JWT token.
     * Returns list of role strings in ROLE_X format.
     *
     * @param token JWT token string
     * @return List of roles, empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getAllClaims(token);
        Object rolesClaim = claims.get(ROLES_CLAIM);

        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        }

        log.warn("Roles claim not found or invalid format in token");
        return List.of();
    }

    /**
     * Extract specific claim from token.
     *
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from token.
     * Parses and validates token signature.
     *
     * @param token JWT token string
     * @return Claims object containing all token claims
     */
    private Claims getAllClaims(String token) {
        byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Validate token expiration and signature.
     *
     * @param token JWT token string
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey))
                .build()
                .parseSignedClaims(token);

            log.debug("Token validation successful");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token is expired");
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid token signature");
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

}

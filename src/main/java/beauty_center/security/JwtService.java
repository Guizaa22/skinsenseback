package beauty_center.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token generation and validation service.
 * Handles both access and refresh token placeholders.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Generate access token from authentication
     */
    public String generateAccessToken(Authentication authentication) {
        // TODO: Extract user details and roles from Authentication object
        // TODO: Add custom claims for roles and permissions
        return generateToken(authentication.getName(), jwtProperties.getExpirationMinutes() * 60);
    }

    /**
     * Generate refresh token (longer expiration)
     */
    public String generateRefreshToken(String username) {
        // TODO: Implement refresh token logic with longer expiration
        return username;
    }

    /**
     * Generate JWT token with specified expiration
     */
    private String generateToken(String subject, long expirationSeconds) {
        byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (expirationSeconds * 1000));

        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }

    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        // TODO: Implement claim extraction with proper error handling
        Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from token
     */
    private Claims getAllClaims(String token) {
        byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Validate token expiration
     */
    public boolean isTokenValid(String token) {
        try {
            byte[] secretKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // TODO: Add proper logging for token validation failures
            return false;
        }
    }

}

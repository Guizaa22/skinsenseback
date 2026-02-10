package beauty_center.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT authentication filter that validates tokens on every request.
 * Stateless - no session storage.
 * Extracts roles from JWT claims and builds Spring Security authorities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);

                // Extract roles from JWT claims
                List<String> roles = jwtService.extractRoles(token);

                // Convert roles to Spring Security GrantedAuthority format
                List<GrantedAuthority> authorities = buildAuthoritiesFromRoles(roles);

                log.debug("Authenticated user: {} with authorities: {}", username, authorities);

                // Create authentication token with extracted roles
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * Expected format: "Bearer <token>"
     *
     * @param request HTTP request
     * @return JWT token string or null if not present/invalid format
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Convert role strings (ROLE_ADMIN format) to Spring Security GrantedAuthority objects.
     *
     * @param roles List of role strings from JWT claims
     * @return List of GrantedAuthority objects
     */
    private List<GrantedAuthority> buildAuthoritiesFromRoles(List<String> roles) {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

}

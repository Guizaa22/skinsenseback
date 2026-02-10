package beauty_center.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Helper component to retrieve current authenticated user from SecurityContext.
 * Thread-safe - uses ThreadLocal in SecurityContext.
 * All methods safely handle null authentication.
 */
@Component
public class CurrentUser {

    /**
     * Get current authenticated username (email).
     *
     * @return Username or null if not authenticated
     */
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Check if user has specific role.
     * Role name is automatically prefixed with "ROLE_" if not already present.
     *
     * @param role Role name (e.g., "ADMIN", "EMPLOYEE", "CLIENT")
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(roleName));
    }

    /**
     * Check if user has any of the specified roles.
     *
     * @param roles Array of role names to check
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all authorities for current user.
     *
     * @return Collection of GrantedAuthority, empty if not authenticated
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : java.util.Collections.emptySet();
    }

    /**
     * Check if user is authenticated.
     *
     * @return true if user is authenticated and not anonymous, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
            && !authentication.getAuthorities().isEmpty();
    }

}

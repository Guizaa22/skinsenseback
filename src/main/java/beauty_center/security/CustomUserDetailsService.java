package beauty_center.security;

import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.security.core.userdetails.User.*;

/**
 * Custom UserDetailsService implementation loading users from database.
 * Constructs Spring Security authorities (GrantedAuthority) from Role enum.
 * Enforces active status - disabled accounts cannot authenticate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        UserAccount user = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", email);
                return new UsernameNotFoundException("User not found with email: " + email);
            });

        // Block access for deactivated accounts
        if (!user.isActive()) {
            log.warn("Login attempt for inactive account: {}", email);
            throw new UsernameNotFoundException("User account is inactive: " + email);
        }

        // Convert Role enum to Spring Security GrantedAuthority
        Collection<GrantedAuthority> authorities = buildAuthorities(user.getRole());

        log.debug("User {} loaded with authorities: {}", email, authorities);

        return builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }

    /**
     * Convert Role enum to Spring Security GrantedAuthority format.
     * Adds "ROLE_" prefix as required by Spring Security conventions.
     */
    private Collection<GrantedAuthority> buildAuthorities(Role role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            // Convert enum value to ROLE_X format: ADMIN -> ROLE_ADMIN
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
            authorities.add(authority);
            log.debug("Added authority: {}", authority.getAuthority());
        } else {
            log.warn("User has null role - no authorities assigned");
        }

        return authorities;
    }

}

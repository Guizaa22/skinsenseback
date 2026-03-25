package beauty_center.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * JWT configuration properties loaded from application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private int expirationMinutes;
    private int refreshExpirationDays;
}

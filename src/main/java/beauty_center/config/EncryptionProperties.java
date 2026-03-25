package beauty_center.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Encryption configuration properties
 */
@Component
@ConfigurationProperties(prefix = "encryption")
@Data
public class EncryptionProperties {

    /**
     * Encryption key for sensitive data
     */
    private String key;

}

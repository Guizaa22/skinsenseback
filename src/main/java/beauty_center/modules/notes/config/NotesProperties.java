package beauty_center.modules.notes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the professional notes module.
 * Bound to 'notes.*' in application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "notes")
public class NotesProperties {

    /**
     * Whether clients can view professional notes on their appointments.
     * Default: false (only employee/admin can view).
     */
    private boolean clientCanView = false;
}

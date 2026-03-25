package beauty_center.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test-specific Spring Boot configuration.
 * Provides test database configuration for H2 in-memory database.
 */
@TestConfiguration
@Profile("test")
public class TestDatabaseConfig {

    // H2 configuration is handled via application-test.yml
    // This class serves as a marker for test-specific configuration

}

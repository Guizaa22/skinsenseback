package beauty_center;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced application startup test with detailed diagnostics.
 * Verifies Spring Boot context loads successfully with test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class ApplicationStartupTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoadsSuccessfully() {
        log.info("Application context loaded successfully");
        assertNotNull(applicationContext, "ApplicationContext should not be null");
    }

    @Test
    void beansAreAvailable() {
        log.info("Checking bean availability");

        assertTrue(applicationContext.containsBean("dataSource"),
            "DataSource bean should be available");
        assertTrue(applicationContext.containsBean("jwtService"),
            "JwtService bean should be available");
        assertTrue(applicationContext.containsBean("authService"),
            "AuthService bean should be available");

        log.info("✓ All required beans are available");
    }

}

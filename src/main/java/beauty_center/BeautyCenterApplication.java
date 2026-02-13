package beauty_center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Beauty Center API - Main Application Entry Point
 *
 * Spring Boot 3.5.x backend for beauty center management system.
 * Package-by-feature architecture for scalable, modular development.
 */
@SpringBootApplication
@EnableScheduling
public class BeautyCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautyCenterApplication.class, args);
    }

}

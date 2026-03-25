package beauty_center.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class for generating BCrypt password hashes for test users.
 * This is a helper tool - do NOT use in production for user registration.
 * <p>
 * Generate test user hashes with:
 * PasswordHashGenerator.main(new String[]{"Admin@123", "Employee@123", "Client@123"})
 */
public class PasswordHashGenerator {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        System.out.println("=== BCrypt Password Hash Generator ===\n");

        String[] testPasswords = {
            "Admin@123",
            "Employee@123",
            "Client@123"
        };

        for (String password : testPasswords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
    }

}

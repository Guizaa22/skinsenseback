package beauty_center;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for test data initialization.
 *
 * This is a reference tool for generating password hashes.
 * Do NOT use in production for user registration - always use PasswordEncoder from Spring Security.
 *
 * Usage:
 * 1. Run this class as Java application
 * 2. Copy the generated hashes
 * 3. Use them in SQL migration files or hardcoded test data
 *
 * Note: BCrypt hashes are non-deterministic - each call generates a different hash
 * but all validate correctly against the original password.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {
            "Admin@123",
            "Employee@123",
            "Client@123"
        };

        System.out.println("BCrypt Password Hash Generator");
        System.out.println("===============================\n");

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
    }
}

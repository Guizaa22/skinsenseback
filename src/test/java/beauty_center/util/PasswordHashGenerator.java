package beauty_center.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for test data
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminPassword = "Admin@123";
        String employeePassword = "Employee@123";
        String clientPassword = "Client@123";

        System.out.println("Admin password hash:");
        System.out.println(encoder.encode(adminPassword));
        System.out.println("\nEmployee password hash:");
        System.out.println(encoder.encode(employeePassword));
        System.out.println("\nClient password hash:");
        System.out.println(encoder.encode(clientPassword));
    }
}

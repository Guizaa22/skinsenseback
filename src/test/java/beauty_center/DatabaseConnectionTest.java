package beauty_center;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnectionSucceeds() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                    assertTrue(resultSet.next(), "Query should return a result");
                    assertEquals(1, resultSet.getInt(1), "Query result should be 1");
                }
            }
        }
    }

    @Test
    void testDatabaseVersion() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT version()")) {
                    if (resultSet.next()) {
                        String version = resultSet.getString(1);
                        assertNotNull(version, "Database version should not be null");
                        // Accept both H2 and PostgreSQL databases
                        boolean isValidDatabase = version.contains("PostgreSQL") || version.contains("H2");
                        assertTrue(isValidDatabase, "Should be PostgreSQL or H2 database, got: " + version);
                        System.out.println("✓ Database Version: " + version);
                    }
                }
            }
        }
    }

    @Test
    void testDatabaseName() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String databaseName = connection.getCatalog();
            assertNotNull(databaseName, "Database name should not be null");
            // Accept H2 test database or PostgreSQL beauty_center_db
            boolean isValidDatabase = "testdb".equalsIgnoreCase(databaseName) ||
                                     "beauty_center_db".equals(databaseName) ||
                                     databaseName.contains("testdb"); // H2 in-memory
            assertTrue(isValidDatabase, "Should be connected to a valid test database, got: " + databaseName);
            System.out.println("✓ Connected to database: " + databaseName);
        }
    }

    @Test
    void testConnectionPoolConfiguration() throws Exception {
        // Test that connection pool is working
        try (Connection conn1 = dataSource.getConnection();
             Connection conn2 = dataSource.getConnection()) {

            assertNotNull(conn1, "First connection should be valid");
            assertNotNull(conn2, "Second connection should be valid");
            assertFalse(conn1.isClosed(), "First connection should be open");
            assertFalse(conn2.isClosed(), "Second connection should be open");

            System.out.println("✓ Connection pool is working correctly");
        }
    }
}

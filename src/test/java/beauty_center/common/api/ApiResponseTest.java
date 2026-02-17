package beauty_center.common.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse Unit Tests")
class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data and message")
    void testSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("test data", "Test message");

        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals("test data", response.getData());
        assertNull(response.getErrorCode());
        assertNull(response.getStatus());
    }

    @Test
    @DisplayName("Should create error response with status code")
    void testErrorResponseWithStatus() {
        ApiResponse<Void> response = ApiResponse.error("Invalid request", 400);

        assertFalse(response.isSuccess());
        assertEquals("Invalid request", response.getMessage());
        assertEquals(400, response.getStatus());
        assertNull(response.getData());
        assertNull(response.getErrorCode());
    }

    @Test
    @DisplayName("Should create error response with error code and status")
    void testErrorResponseWithErrorCode() {
        ApiResponse<Void> response = ApiResponse.error("Invalid argument", "INVALID_ARGUMENT", 400);

        assertFalse(response.isSuccess());
        assertEquals("Invalid argument", response.getMessage());
        assertEquals("INVALID_ARGUMENT", response.getErrorCode());
        assertEquals(400, response.getStatus());
        assertNull(response.getData());
    }
}


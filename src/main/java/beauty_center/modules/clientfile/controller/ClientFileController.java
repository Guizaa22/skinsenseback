package beauty_center.modules.clientfile.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.clientfile.dto.ClientFileResponse;
import beauty_center.modules.clientfile.dto.ClientFileUpdateRequest;
import beauty_center.modules.clientfile.entity.ClientFile;
import beauty_center.modules.clientfile.service.ClientFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Client file REST controller (sensitive medical data).
 * Implements strict access control:
 * - Clients can read/write their own file via /api/client/me/file
 * - Employees/Admins can read any client file via /api/clients/{id}/file
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientFileController {

    private final ClientFileService clientFileService;

    /**
     * Get current client's own file.
     * Creates file if it doesn't exist.
     *
     * @return ClientFileResponse
     */
    @GetMapping("/api/client/me/file")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientFileResponse>> getMyClientFile() {
        log.info("GET /api/client/me/file - Getting current client's file");

        ClientFile clientFile = clientFileService.getMyClientFile();
        ClientFileResponse response = clientFileService.toResponse(clientFile);

        return ResponseEntity.ok(ApiResponse.ok(response, "Client file retrieved successfully"));
    }

    /**
     * Update current client's own file.
     * Only clients can update their own declarative sections.
     *
     * @param request Update request with new data
     * @return Updated ClientFileResponse
     */
    @PutMapping("/api/client/me/file")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientFileResponse>> updateMyClientFile(
            @RequestBody ClientFileUpdateRequest request) {
        log.info("PUT /api/client/me/file - Updating current client's file");

        ClientFile updated = clientFileService.updateMyClientFile(request);
        ClientFileResponse response = clientFileService.toResponse(updated);

        return ResponseEntity.ok(ApiResponse.ok(response, "Client file updated successfully"));
    }

    /**
     * Get client file by client ID (Employee/Admin only).
     * Employees and admins can read any client's file.
     *
     * @param clientId UUID of the client
     * @return ClientFileResponse or 404 if not found
     */
    @GetMapping("/api/clients/{clientId}/file")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClientFileResponse>> getClientFile(@PathVariable UUID clientId) {
        log.info("GET /api/clients/{}/file - Getting client file (staff access)", clientId);

        Optional<ClientFile> clientFile = clientFileService.getClientFile(clientId);

        if (clientFile.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Client file not found for client: " + clientId, 404));
        }

        ClientFileResponse response = clientFileService.toResponse(clientFile.get());
        return ResponseEntity.ok(ApiResponse.ok(response, "Client file retrieved successfully"));
    }
}

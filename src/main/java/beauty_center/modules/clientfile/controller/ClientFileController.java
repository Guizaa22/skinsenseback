package beauty_center.modules.clientfile.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.clientfile.entity.ClientFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Client file REST controller (sensitive medical data)
 */
@RestController
@RequestMapping("/api/client-files")
@RequiredArgsConstructor
public class ClientFileController {

    // TODO: Inject ClientFileService

    @GetMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientFile>> getClientFile(@PathVariable UUID clientId) {
        // TODO: Get with access control
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientFile>> updateClientFile(
            @PathVariable UUID clientId,
            @RequestBody ClientFile updates) {
        // TODO: Update with audit logging
        return ResponseEntity.ok(ApiResponse.ok(null, "Client file updated"));
    }

}

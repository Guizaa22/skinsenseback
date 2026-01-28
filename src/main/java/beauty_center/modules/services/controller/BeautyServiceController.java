package beauty_center.modules.services.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.services.dto.BeautyServiceCreateRequest;
import beauty_center.modules.services.dto.BeautyServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Beauty service REST controller
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class BeautyServiceController {

    // TODO: Inject BeautyServiceService

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getServices() {
        // TODO: Return all active services
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> getServiceById(@PathVariable UUID id) {
        // TODO: Get service by ID
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> createService(
            @Valid @RequestBody BeautyServiceCreateRequest request) {
        // TODO: Create new service
        return ResponseEntity.ok(ApiResponse.ok(null, "Service created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeautyServiceResponse>> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody BeautyServiceCreateRequest request) {
        // TODO: Update service
        return ResponseEntity.ok(ApiResponse.ok(null, "Service updated"));
    }

}

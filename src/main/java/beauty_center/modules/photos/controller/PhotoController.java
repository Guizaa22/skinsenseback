package beauty_center.modules.photos.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.photos.entity.ClientPhoto;
import beauty_center.modules.photos.entity.ServicePhoto;
import beauty_center.modules.photos.service.PhotoService;
import beauty_center.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final CurrentUser currentUser;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // ── Client photos ──────────────────────────────────────────

    @PostMapping("/clients/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadClientPhoto(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "before") String label,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        ClientPhoto photo = photoService.uploadClientPhoto(
                clientId, currentUser.getUserId(), label, file);
        return ResponseEntity.ok(ApiResponse.ok(toClientDto(photo), "Photo uploaded"));
    }

    @GetMapping("/clients/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE') or @currentUser.getUserId() == #clientId")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClientPhotos(@PathVariable UUID clientId) {
        List<Map<String, Object>> photos = photoService.getClientPhotos(clientId).stream()
                .map(this::toClientDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(photos, "OK"));
    }

    @GetMapping("/clients/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyPhotos() {
        List<Map<String, Object>> photos = photoService.getClientPhotos(currentUser.getUserId())
                .stream().map(this::toClientDto).toList();
        return ResponseEntity.ok(ApiResponse.ok(photos, "OK"));
    }

    @DeleteMapping("/clients/photo/{photoId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> deleteClientPhoto(@PathVariable UUID photoId) {
        photoService.deleteClientPhoto(photoId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    // ── Service photos ─────────────────────────────────────────

    @PostMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadServicePhoto(
            @PathVariable UUID serviceId,
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        ServicePhoto photo = photoService.uploadServicePhoto(serviceId, currentUser.getUserId(), file);
        return ResponseEntity.ok(ApiResponse.ok(toServiceDto(photo), "Photo uploaded"));
    }

    @GetMapping("/services/{serviceId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getServicePhotos(@PathVariable UUID serviceId) {
        List<Map<String, Object>> photos = photoService.getServicePhotos(serviceId).stream()
                .map(this::toServiceDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(photos, "OK"));
    }

    @DeleteMapping("/services/photo/{photoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServicePhoto(@PathVariable UUID photoId) {
        photoService.deleteServicePhoto(photoId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Deleted"));
    }

    // ── File serving ───────────────────────────────────────────

    @GetMapping("/file/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        try {
            String path = request.getRequestURI().replaceFirst(".*/api/photos/file/", "");
            Path filePath = Paths.get(uploadDir).resolve(path).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── DTOs ───────────────────────────────────────────────────

    private Map<String, Object> toClientDto(ClientPhoto p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("clientId", p.getClientId());
        m.put("label", p.getLabel());
        m.put("fileName", p.getFileName());
        m.put("url", "/api/photos/file/" + p.getFilePath());
        m.put("contentType", p.getContentType());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> toServiceDto(ServicePhoto p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("serviceId", p.getServiceId());
        m.put("fileName", p.getFileName());
        m.put("url", "/api/photos/file/" + p.getFilePath());
        m.put("contentType", p.getContentType());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }
}

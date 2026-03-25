package beauty_center.modules.photos.service;

import beauty_center.modules.photos.entity.ClientPhoto;
import beauty_center.modules.photos.entity.ServicePhoto;
import beauty_center.modules.photos.repository.ClientPhotoRepository;
import beauty_center.modules.photos.repository.ServicePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {

    private final ClientPhotoRepository clientPhotoRepo;
    private final ServicePhotoRepository servicePhotoRepo;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private String saveFile(MultipartFile file, String subDir) throws IOException {
        Path dir = Paths.get(uploadDir, subDir);
        Files.createDirectories(dir);
        String sanitized = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        String fileName = UUID.randomUUID() + "_" + sanitized;
        Path target = dir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return subDir + "/" + fileName;
    }

    public ClientPhoto uploadClientPhoto(UUID clientId, UUID uploadedBy,
                                         String label, MultipartFile file) throws IOException {
        String path = saveFile(file, "clients/" + clientId);
        ClientPhoto photo = ClientPhoto.builder()
                .clientId(clientId)
                .uploadedBy(uploadedBy)
                .label(label != null ? label : "before")
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                .filePath(path)
                .contentType(file.getContentType())
                .build();
        return clientPhotoRepo.save(photo);
    }

    @Transactional(readOnly = true)
    public List<ClientPhoto> getClientPhotos(UUID clientId) {
        return clientPhotoRepo.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public void deleteClientPhoto(UUID photoId) {
        clientPhotoRepo.findById(photoId).ifPresent(p -> {
            try {
                Files.deleteIfExists(Paths.get(uploadDir, p.getFilePath()));
            } catch (IOException ignored) {
            }
            clientPhotoRepo.delete(p);
        });
    }

    public ServicePhoto uploadServicePhoto(UUID serviceId, UUID uploadedBy,
                                           MultipartFile file) throws IOException {
        String path = saveFile(file, "services/" + serviceId);
        ServicePhoto photo = ServicePhoto.builder()
                .serviceId(serviceId)
                .uploadedBy(uploadedBy)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                .filePath(path)
                .contentType(file.getContentType())
                .build();
        return servicePhotoRepo.save(photo);
    }

    @Transactional(readOnly = true)
    public List<ServicePhoto> getServicePhotos(UUID serviceId) {
        return servicePhotoRepo.findByServiceIdOrderByCreatedAtDesc(serviceId);
    }

    public void deleteServicePhoto(UUID photoId) {
        servicePhotoRepo.findById(photoId).ifPresent(p -> {
            try {
                Files.deleteIfExists(Paths.get(uploadDir, p.getFilePath()));
            } catch (IOException ignored) {
            }
            servicePhotoRepo.delete(p);
        });
    }
}

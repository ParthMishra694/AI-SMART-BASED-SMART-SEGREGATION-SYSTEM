package com.wastewise.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp", "jfif", "bmp", "gif", "heic", "heif"
    );

    public StorageService() {
        try {
            if (!Files.exists(UPLOAD_DIR)) {
                Files.createDirectories(UPLOAD_DIR);
            }
        } catch (IOException e) {
            log.error("Failed to initialize upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file. Please select a valid photo.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "waste_item.jpg";
        }

        String ext = "jpg";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx < originalFilename.length() - 1) {
            String parsedExt = originalFilename.substring(dotIdx + 1).toLowerCase().trim();
            if (ALLOWED_EXTENSIONS.contains(parsedExt)) {
                ext = parsedExt;
            }
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerType = contentType.toLowerCase().trim();
            if (!lowerType.startsWith("image/") && !lowerType.equals("application/octet-stream")) {
                throw new IllegalArgumentException("Invalid file format. Please upload an image file (JPG, PNG, WEBP, etc.).");
            }
        }

        String storedFilename = UUID.randomUUID().toString() + "." + ext;
        Path destinationPath = UPLOAD_DIR.resolve(storedFilename).normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored uploaded file: {} -> {}", originalFilename, storedFilename);
        return "/uploads/" + storedFilename;
    }
}

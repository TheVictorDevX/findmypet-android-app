package com.example.findmypetbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Original filename cannot be null or empty.");
        }
        // Sanitize filename to prevent security issues (path traversal, etc.)
        String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        
        // Generate a unique filename to prevent collisions
        String fileName = UUID.randomUUID().toString() + "_" + sanitizedFileName;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            return fileName;
        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", fileName, ex);
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public boolean deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            // Security check: ensure the resolved path is within the designated directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                logger.warn("Attempted to delete a file outside of the storage directory: {}", filename);
                return false;
            }
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Could not delete file: {}", filename, e);
            return false;
        }
    }
}
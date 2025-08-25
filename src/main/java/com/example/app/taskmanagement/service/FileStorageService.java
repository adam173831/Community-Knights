package com.example.app.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.file-storage.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    public void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("Upload directory initialized: {}", uploadPath);
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(InputStream inputStream, String originalFileName) {
        try {
            if (uploadPath == null) {
                init();
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path targetPath = uploadPath.resolve(uniqueFileName);
            
            // Ensure the file doesn't already exist
            if (Files.exists(targetPath)) {
                uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                targetPath = uploadPath.resolve(uniqueFileName);
            }

            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File stored successfully: {}", uniqueFileName);
            return uniqueFileName;
            
        } catch (IOException e) {
            logger.error("Error storing file: {}", originalFileName, e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            if (uploadPath == null) {
                init();
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.deleteIfExists(filePath);
            logger.info("File deleted: {}", fileName);
            
        } catch (IOException e) {
            logger.error("Error deleting file: {}", fileName, e);
        }
    }

    public boolean fileExists(String fileName) {
        if (uploadPath == null) {
            init();
        }
        return Files.exists(uploadPath.resolve(fileName));
    }

    public Path getFilePath(String fileName) {
        if (uploadPath == null) {
            init();
        }
        return uploadPath.resolve(fileName);
    }

    public long getFileSize(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            return Files.size(filePath);
        } catch (IOException e) {
            logger.error("Error getting file size: {}", fileName, e);
            return 0;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex);
    }

    public boolean isValidImageFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    public boolean isValidDataFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.matches("\\.(json|csv|xlsx|xml)$");
    }
}
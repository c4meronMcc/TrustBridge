package com.trustbridge.Features.Jobs.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final String STORAGE_DIRECTORY = "uploads/milestones/";

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String extension = getFileExtension(file.getOriginalFilename());

        if (!checkFileTypes(extension)) throw new IllegalArgumentException("Invalid file type");

        log.info("File type: " + extension);

        try {
            Path pathDirectory = Paths.get(STORAGE_DIRECTORY);
            if (!pathDirectory.toFile().exists()) {
                Files.createDirectories(pathDirectory);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = pathDirectory.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error creating directory: {}", e.getMessage());
            throw new IOException("Failed to create directory: " + e.getMessage());
        }
    }

    private boolean checkFileTypes(String extension) {

        switch (extension.toLowerCase()) {
            case "pdf":
                return true;
            case "doc":
                return true;
            case "docx":
                return true;
            case "txt":
                return true;
            case "jpg":
                return true;
            case "jpeg":
                return true;
            case "png":
                return true;
            default:
                throw new IllegalArgumentException("Invalid file type");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        Path path = Paths.get(fileName);
        String name = path.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');

        // Ensure dot exists and is not at the start or end
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return null;
    }
}

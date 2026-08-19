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
}

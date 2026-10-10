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
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        String extension = getFileExtension(file.getOriginalFilename());



        if (!checkFileTypes(extension, file.getInputStream().readNBytes(512))) throw new IllegalArgumentException("Invalid file type");

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

    private boolean startsWith(byte[] header, int... signature) {
        if (header.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((header[i] & 0xFF) != signature[i]) return false;
        }
        return true;
    }

    private boolean checkFileTypes(String extension, byte[] header) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return startsWith(header, 0x25, 0x50, 0x44, 0x46);
            case "png":
                return startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "jpg":
            case "jpeg":
                return startsWith(header, 0xFF, 0xD8, 0xFF);
            case "doc":
                return startsWith(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "docx":
                return startsWith(header, 0x50, 0x4B, 0x03, 0x04);
            case "txt":
                for (byte b : header) {
                    if (b == 0) return false;
                }
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
        throw new IllegalArgumentException("Invalid file name format");
    }
}

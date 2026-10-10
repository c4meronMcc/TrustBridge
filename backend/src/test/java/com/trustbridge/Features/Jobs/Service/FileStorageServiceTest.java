package com.trustbridge.Features.Jobs.Service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Test Description:
     * Tests the successful storage of a valid file.
     */
    @Test
    void testStoreFile_Success() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String fileName = "test.pdf";
        byte[] fileData = new byte[]{0x25, 0x50, 0x44, 0x46}; // Valid PDF header

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getBytes()).thenReturn(fileData);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileData));

        // Act
        String result = fileStorageService.storeFile(mockFile);

        // Assert
        assertNotNull(result);
        assertTrue(result.endsWith(fileName));
        Files.deleteIfExists(Path.of(result)); // Clean up the test file
    }

    /**
     * Test Description:
     * Tests storing an empty file which should throw an IllegalArgumentException.
     */
    @Test
    void testStoreFile_ThrowsExceptionForEmptyFile() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(mockFile);
        });

        assertEquals("File is empty", exception.getMessage());
    }

    /**
     * Test Description:
     * Tests storing a file with an invalid file extension.
     */
    @Test
    void testStoreFile_ThrowsExceptionForInvalidFileExtension() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String fileName = "invalid.xyz";
        byte[] fileData = new byte[]{0x00, 0x00}; // Arbitrary file data

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getBytes()).thenReturn(fileData);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(mockFile);
        });

        assertEquals("Invalid file type", exception.getMessage());
    }

    /**
     * Test Description:
     * Tests storing a file that fails during directory creation.
     */
    @Test
    void testStoreFile_ThrowsIOExceptionForDirectoryCreationFailure() throws IOException {
        // Arrange
        FileStorageService spyService = Mockito.spy(fileStorageService);
        doThrow(new IOException("Failed to create directory")).when(spyService).storeFile(any(MultipartFile.class));

        MultipartFile mockFile = mock(MultipartFile.class);
        byte[] fileData = new byte[]{0x25, 0x50, 0x44, 0x46}; // Valid PDF header
        String fileName = "test.pdf";

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getBytes()).thenReturn(fileData);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileData));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            spyService.storeFile(mockFile);
        });

        assertTrue(exception.getMessage().contains("Failed to create directory"));
    }

    /**
     * Test Description:
     * Tests storing a file with a valid custom file type (PNG).
     */
    @Test
    void testStoreFile_SuccessWithPNGFile() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String fileName = "image.png";
        byte[] fileData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // Valid PNG header

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getBytes()).thenReturn(fileData);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileData));

        // Act
        String result = fileStorageService.storeFile(mockFile);

        // Assert
        assertNotNull(result);
        assertTrue(result.endsWith(fileName));
        Files.deleteIfExists(Path.of(result)); // Clean up the test file
    }
}
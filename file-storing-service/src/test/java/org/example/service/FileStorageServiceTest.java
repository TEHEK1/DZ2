package org.example.service;

import org.example.dto.FilePlagiarismResponseDTO;
import org.example.dto.FileUploadResponseDTO;
import org.example.exception.FileMetadataNotFoundException;
import org.example.model.FileMetadata;
import org.example.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    private FileStorageService fileStorageService;
    private MockMultipartFile testFile;
    private FileMetadata testFileMetadata;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-uploads");
        fileStorageService = new FileStorageService(fileMetadataRepository, tempDir);

        testFile = new MockMultipartFile(
            "test.txt",
            "test.txt",
            "text/plain",
            "Hello, World!".getBytes()
        );

        testFileMetadata = new FileMetadata();
        testFileMetadata.setId(1L);
        testFileMetadata.setName("test.txt");
        testFileMetadata.setHash("test-hash");
        testFileMetadata.setLocation(tempDir.resolve("test.txt").toString());
    }

    @Test
    void storeFile_ShouldSaveFileAndReturnResponse() throws IOException {
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(testFileMetadata);

        FileUploadResponseDTO response = fileStorageService.storeFile(testFile);

        assertNotNull(response);
        assertEquals(testFileMetadata.getId(), response.getId());
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    void checkPlagiarism_WhenFileExists_ShouldReturnPlagiarismInfo() {
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        when(fileMetadataRepository.findByHash(anyString())).thenReturn(Arrays.asList(testFileMetadata));

        FilePlagiarismResponseDTO response = fileStorageService.checkPlagiarism(1L);

        assertNotNull(response);
        verify(fileMetadataRepository).findById(1L);
        verify(fileMetadataRepository).findByHash(anyString());
    }

    @Test
    void checkPlagiarism_WhenFileNotFound_ShouldThrowException() {
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FileMetadataNotFoundException.class, () -> fileStorageService.checkPlagiarism(1L));
    }

    @Test
    void loadFileAsResource_WhenFileExists_ShouldReturnFileResource() throws IOException {
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(testFileMetadata));
        Files.write(tempDir.resolve("test.txt"), "Hello, World!".getBytes());

        var response = fileStorageService.loadFileAsResource(1L);

        assertNotNull(response);
        assertEquals(testFileMetadata.getName(), response.getFilename());
        assertNotNull(response.getInputStream());
    }

    @Test
    void loadFileAsResource_WhenFileNotFound_ShouldThrowException() {
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FileMetadataNotFoundException.class, () -> fileStorageService.loadFileAsResource(1L));
    }
} 
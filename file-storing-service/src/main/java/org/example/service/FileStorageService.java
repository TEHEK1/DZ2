package org.example.service;

import org.example.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.example.model.FileMetadata;
import org.example.dto.FileUploadResponseDTO;
import org.example.dto.FileResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Autowired
    public FileStorageService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public FileUploadResponseDTO storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileHash = calculateHash(file);

        List<FileMetadata> existingFiles = fileMetadataRepository.findByHash(fileHash);

        for (FileMetadata existingFile : existingFiles) {
            Path existingFilePath = Paths.get(existingFile.getLocation()).normalize();
            if (Files.exists(existingFilePath) && areFilesContentEqual(file, existingFilePath)) {
                return convertToUploadResponseDTO(existingFile);
            }
        }

        String filename = System.currentTimeMillis() + "_" + originalFilename;
        Path targetLocation = this.fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setName(originalFilename);
        fileMetadata.setHash(fileHash);
        fileMetadata.setLocation(targetLocation.toString());
        FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);

        return convertToUploadResponseDTO(savedMetadata);
    }

    private String calculateHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not calculate file hash", e);
        }
    }

    private boolean areFilesContentEqual(MultipartFile uploadedFile, Path existingFilePath) {
        try (InputStream uploadedStream = uploadedFile.getInputStream();
             InputStream existingStream = Files.newInputStream(existingFilePath)) {

            byte[] uploadedBytes = uploadedStream.readAllBytes();
            byte[] existingBytes = existingStream.readAllBytes();

            return MessageDigest.isEqual(uploadedBytes, existingBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error comparing file contents", e);
        }
    }

    public FileResource loadFileAsResource(Long fileId) throws IOException {
        Optional<FileMetadata> metadata = fileMetadataRepository.findById(fileId);
        if (metadata.isPresent()) {
            FileMetadata fileMetadata = metadata.get();
            Path filePath = Paths.get(fileMetadata.getLocation()).normalize();
            if (Files.exists(filePath)) {
                InputStream inputStream = Files.newInputStream(filePath);
                return new FileResource(inputStream, fileMetadata.getName());
            } else {
                 throw new RuntimeException("File not found on disk for id " + fileId);
            }
        } else {
            throw new RuntimeException("File metadata not found with id " + fileId);
        }
    }

    private FileUploadResponseDTO convertToUploadResponseDTO(FileMetadata metadata) {
        FileUploadResponseDTO dto = new FileUploadResponseDTO();
        dto.setId(metadata.getId());
        return dto;
    }
}

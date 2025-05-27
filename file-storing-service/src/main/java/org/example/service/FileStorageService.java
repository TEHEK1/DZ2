package org.example.service;

import jakarta.annotation.Nullable;
import org.example.dto.FilePlagiarismResponseDTO;
import org.example.exception.FileMetadataNotFoundException;
import org.example.exception.FileNotFoundException;
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

@Service
public class FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final Path fileStorageLocation = Paths.get("/app/uploads").toAbsolutePath().normalize();

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

    public FilePlagiarismResponseDTO checkPlagiarism(Long fileId) {
        Optional<FileMetadata> metadata = fileMetadataRepository.findById(fileId);
        if (metadata.isPresent()) {
            FileMetadata fileMetadata = metadata.get();
            String fileHash = fileMetadata.getHash();
            List<FileMetadata> existingFiles = fileMetadataRepository.findByHash(fileHash);
            for (FileMetadata existingFile : existingFiles) {
                Path filePath = this.fileStorageLocation.resolve(fileMetadata.getLocation());
                Path existingFilePath = this.fileStorageLocation.resolve(existingFile.getLocation());
                if (!fileId.equals(existingFile.getId()) && areFilesContentEqual(filePath, existingFilePath)) {
                    return convertToPlagiarismResponseDTO(existingFile);
                }
            }
            return convertToPlagiarismResponseDTO(null);
        } else {
            throw new FileMetadataNotFoundException("File metadata not found with id " + fileId);
        }
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

    private boolean areFilesContentEqual(Path currentFilePath, Path otherFilePath) {
        try {
            byte[] currentBytes = Files.readAllBytes(currentFilePath);
            byte[] otherBytes = Files.readAllBytes(otherFilePath);
            return MessageDigest.isEqual(currentBytes, otherBytes);
        } catch (IOException e) {
            System.err.println("Error comparing file contents: " + e.getMessage());
            return false;
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
                throw new FileNotFoundException("File not found on disk for id " + fileId, 
                    new IOException("File does not exist at path: " + filePath));
            }
        } else {
            throw new FileMetadataNotFoundException("File metadata not found with id " + fileId);
        }
    }

    private FileUploadResponseDTO convertToUploadResponseDTO(FileMetadata metadata) {
        FileUploadResponseDTO dto = new FileUploadResponseDTO();
        dto.setId(metadata.getId());
        return dto;
    }

    private FilePlagiarismResponseDTO convertToPlagiarismResponseDTO(@Nullable FileMetadata plagiarizedFile) {
        FilePlagiarismResponseDTO dto = new FilePlagiarismResponseDTO();
        dto.setPlagiarismFileId(plagiarizedFile != null ? plagiarizedFile.getId() : null);
        return dto;
    }
}

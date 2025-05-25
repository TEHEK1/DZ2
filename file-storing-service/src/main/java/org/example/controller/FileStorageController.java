package org.example.controller;

import org.example.dto.FilePlagiarismResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.dto.FileUploadResponseDTO;
import org.example.service.FileStorageService;
import org.example.dto.FileResource;

import java.io.IOException;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/files")
@Tag(name = "File Storage", description = "API for storing and retrieving files")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Upload a text file", description = "Uploads a new text file and returns its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "New file uploaded successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = FileUploadResponseDTO.class))),
        @ApiResponse(responseCode = "200", description = "File already exists",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = FileUploadResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file format or empty file"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file processing")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadFile(
            @Parameter(description = "The text file to upload")
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().split("\\.")[file.getOriginalFilename().toLowerCase().split("\\.").length - 1], "txt")) {
             return ResponseEntity.badRequest().body(null);
        }

        try {
            FileUploadResponseDTO response = fileStorageService.storeFile(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Get file content by ID", description = "Retrieves the content of a file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File retrieved successfully",
                     content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                                        schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file retrieval")
    })
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "ID of the file to retrieve", required = true)
            @PathVariable Long id) {
        try {
            FileResource fileResource = fileStorageService.loadFileAsResource(id);
            byte[] fileContent = fileResource.getInputStream().readAllBytes();
            String filename = fileResource.getFilename();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/plagiarism/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FilePlagiarismResponseDTO> getPlagiarism(
            @Parameter(description = "ID of the file to retrieve", required = true)
            @PathVariable Long id) {
        try {
            FilePlagiarismResponseDTO response = fileStorageService.checkPlagiarism(id);

            return ResponseEntity.ok()
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
} 
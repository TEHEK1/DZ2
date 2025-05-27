package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.AnalysisResponseDTO;
import org.example.service.FileAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/analysis")
@Tag(name = "File Analysis", description = "API for retrieving files analysis")
public class FileAnalysisController {

    private final FileAnalysisService fileAnalysisService;

    @Value("${wordcloud.storage.path:wordclouds}")
    private String wordCloudStoragePath;

    @Autowired
    public FileAnalysisController(FileAnalysisService fileAnalysisService) {
        this.fileAnalysisService = fileAnalysisService;
    }

    @Operation(summary = "Get file analysis by ID", description = "Retrieves the analysis of a file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File analysed successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                             schema = @Schema(implementation = AnalysisResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file retrieval")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalysisResponseDTO> getAnalysis(
            @Parameter(description = "ID of the file to retrieve", required = true)
            @PathVariable Long id) {
        try {
            AnalysisResponseDTO response = fileAnalysisService.analyzeFile(id);
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Get word cloud image", description = "Retrieves the word cloud image for a file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Word cloud image retrieved successfully",
                     content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
        @ApiResponse(responseCode = "404", description = "Word cloud image not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during image retrieval")
    })
    @GetMapping(value = "/wordcloud/{filename}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getWordCloudImage(
            @Parameter(description = "Name of the word cloud image file", required = true)
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(wordCloudStoragePath, filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 
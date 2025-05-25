package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.AnalysisResponseDTO;
import org.example.service.FileAnalysisService;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/analysis")
@Tag(name = "File Analysis", description = "API for retrieving files analysis")
public class FileAnalysisController {

    private final FileAnalysisService fileAnalysisService;

    @Autowired
    public FileAnalysisController(FileAnalysisService fileAnalysisService) {
        this.fileAnalysisService = fileAnalysisService;
    }

    @Operation(summary = "Get file content by ID", description = "Retrieves the analysis of a file by its ID")
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
            AnalysisResponseDTO response = fileAnalysisService.analyseFileByID(id);

            return ResponseEntity.ok()
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
} 
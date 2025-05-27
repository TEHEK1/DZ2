package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.FilePlagiarismResponseDTO;
import org.example.dto.FileUploadResponseDTO;
import org.example.dto.AnalysisResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping("/api")
@Tag(name = "API Gateway", description = "API для работы с файлами и их анализом")
public class ApiGatewayController {

    private final RestTemplate restTemplate;
    
    @Value("${file-storage.service.url}")
    private String fileStorageServiceUrl;
    
    @Value("${file-analysis.service.url}")
    private String fileAnalysisServiceUrl;

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

    public ApiGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Upload a text file", description = "Uploads a new text file and returns its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "New file uploaded successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = FileUploadResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file format or empty file"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file processing")
    })
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
        } catch (IOException e) {
            logger.error("Error while reading file bytes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while reading file: " + e.getMessage());
        }
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            return restTemplate.exchange(
                fileStorageServiceUrl + "/files/upload",
                HttpMethod.POST,
                requestEntity,
                Object.class
            );
        } catch (Exception e) {
            logger.error("Error while uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while uploading file: " + e.getMessage());
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
    @GetMapping("/files/{id}")
    public ResponseEntity<?> getFile(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        try {
            return restTemplate.exchange(
                fileStorageServiceUrl + "/files/" + id,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
        } catch (Exception e) {
            logger.error("Error while getting file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while getting file: " + e.getMessage());
        }
    }

    @Operation(summary = "Get file plagiarism by ID", description = "Retrieves the plagiarism of a file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File plagiarism retrieved successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = FilePlagiarismResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file plagiarism retrieval")
    })
    @GetMapping("/files/plagiarism/{id}")
    public ResponseEntity<?> checkPlagiarism(@PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        try {
            return restTemplate.exchange(
                fileStorageServiceUrl + "/files/plagiarism/" + id,
                HttpMethod.GET,
                requestEntity,
                Object.class
            );
        } catch (Exception e) {
            logger.error("Error while checking plagiarism: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error occurred while checking plagiarism: " + e.getMessage());
        }
    }

    @Operation(summary = "Get file analysis by ID", description = "Retrieves the analysis of a file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File analysed successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                             schema = @Schema(implementation = AnalysisResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file retrieval")
    })
    @GetMapping("/analysis/{id}")
    public ResponseEntity<?> analyzeFile(@PathVariable Long id) {
        return restTemplate.getForEntity(fileAnalysisServiceUrl + "/analysis/" + id, Object.class);
    }
} 
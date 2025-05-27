package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Upload file response")
public class FileUploadResponseDTO {
    @Schema(description = "Uploaded file ID")
    private Long id;
} 
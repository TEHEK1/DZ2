package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "File analysis response")
public class AnalysisResponseDTO {
    private Long paragraphCount;
    private Long wordCount;
    private Long characterCount;
    private Long plagiarismFileId;
} 
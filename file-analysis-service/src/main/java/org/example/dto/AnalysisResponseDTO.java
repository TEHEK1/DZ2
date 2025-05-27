package org.example.dto;

import lombok.Data;

@Data
public class AnalysisResponseDTO {
    private Long fileId;
    private Long paragraphCount;
    private Long wordCount;
    private Long characterCount;
    private Long plagiarismFileId;
}
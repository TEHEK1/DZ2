package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "File plagiarism response")
@Data
public class FilePlagiarismResponseDTO {
    @Schema(description = "Plagiarized file ID")
    private Long plagiarismFileId;
} 
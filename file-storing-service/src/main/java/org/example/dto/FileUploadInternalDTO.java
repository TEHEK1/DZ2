package org.example.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadInternalDTO extends FileUploadResponseDTO {
    private boolean newFile;
} 
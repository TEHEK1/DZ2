package org.example.service;

import org.example.repository.AnalysisMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.model.AnalysisMetadata;
import org.example.dto.AnalysisResponseDTO;

import java.io.IOException;
import java.util.Optional;

@Service
public class FileAnalysisService {

    private final AnalysisMetadataRepository analysisMetadataRepository;

    @Autowired
    public FileAnalysisService(AnalysisMetadataRepository analysisMetadataRepository) {
        this.analysisMetadataRepository = analysisMetadataRepository;
    }

    public AnalysisResponseDTO analyseFileByID(Long fileId) throws IOException {
        Optional<AnalysisMetadata> metadata = analysisMetadataRepository.findById(fileId);
        if (metadata.isPresent()) {
            AnalysisMetadata analysisMetadata = metadata.get();
            return convertToUploadResponseDTO(analysisMetadata);
        } else {
            return new AnalysisResponseDTO();
        }

    }

    private AnalysisResponseDTO convertToUploadResponseDTO(AnalysisMetadata metadata) {
        AnalysisResponseDTO dto = new AnalysisResponseDTO();
        dto.setId(metadata.getId());
        return dto;
    }
}

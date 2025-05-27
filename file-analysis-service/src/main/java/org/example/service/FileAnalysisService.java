package org.example.service;

import org.example.dto.AnalysisResponseDTO;
import org.example.dto.FilePlagiarismResponseDTO;
import org.example.model.AnalysisMetadata;
import org.example.repository.AnalysisMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

@Service
public class FileAnalysisService {
    private final AnalysisMetadataRepository analysisMetadataRepository;
    private final RestTemplate restTemplate;
    
    @Value("${file-storage.service.url}")
    private String fileStorageServiceUrl;

    public FileAnalysisService(AnalysisMetadataRepository analysisMetadataRepository) {
        this.analysisMetadataRepository = analysisMetadataRepository;
        this.restTemplate = new RestTemplate();
    }

    public AnalysisResponseDTO analyzeFile(Long fileId) {
        Optional<AnalysisMetadata> existingAnalysis = analysisMetadataRepository.findByFileId(fileId);
        if (existingAnalysis.isPresent()) {
            return convertToResponseDTO(existingAnalysis.get());
        }

        try {
            String fileUrl = fileStorageServiceUrl + "/files/" + fileId;
            URL url = new URL(fileUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            
            AnalysisMetadata metadata = new AnalysisMetadata();
            metadata.setFileId(fileId);
            
            String line;
            StringBuilder content = new StringBuilder();
            int paragraphCount = 0;
            int wordCount = 0;
            int characterCount = 0;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                if (line.trim().isEmpty()) {
                    paragraphCount++;
                }
                wordCount += line.split("\\s+").length;
                characterCount += line.length();
            }
            
            if (!content.toString().trim().isEmpty()) {
                paragraphCount++;
            }
            
            metadata.setParagraphCount((long) paragraphCount);
            metadata.setWordCount((long) wordCount);
            metadata.setCharacterCount((long) characterCount);
            
            FilePlagiarismResponseDTO plagiarismResponse = restTemplate.getForObject(
                fileStorageServiceUrl + "/files/plagiarism/" + fileId,
                FilePlagiarismResponseDTO.class
            );
            
            if (plagiarismResponse != null && plagiarismResponse.getPlagiarismFileId() != null) {
                metadata.setPlagiarismFileId(plagiarismResponse.getPlagiarismFileId());
            }
            
            AnalysisMetadata savedMetadata = analysisMetadataRepository.save(metadata);
            return convertToResponseDTO(savedMetadata);
            
        } catch (IOException e) {
            throw new RuntimeException("Error analyzing file: " + e.getMessage(), e);
        }
    }

    private AnalysisResponseDTO convertToResponseDTO(AnalysisMetadata metadata) {
        AnalysisResponseDTO response = new AnalysisResponseDTO();
        response.setParagraphCount(metadata.getParagraphCount());
        response.setWordCount(metadata.getWordCount());
        response.setCharacterCount(metadata.getCharacterCount());
        response.setPlagiarismFileId(metadata.getPlagiarismFileId());
        return response;
    }
}

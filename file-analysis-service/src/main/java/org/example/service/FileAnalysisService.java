package org.example.service;

import org.example.dto.AnalysisResponseDTO;
import org.example.dto.FilePlagiarismResponseDTO;
import org.example.model.AnalysisMetadata;
import org.example.repository.AnalysisMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FileAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(FileAnalysisService.class);
    
    private final AnalysisMetadataRepository analysisMetadataRepository;
    private final RestTemplate restTemplate;
    private final String wordCloudStoragePath;
    
    @Value("${file-storage.service.url}")
    private String fileStorageServiceUrl;

    @Value("${quickchart.api.url:https://quickchart.io/wordcloud}")
    private String quickChartApiUrl;

    @Autowired
    public FileAnalysisService(AnalysisMetadataRepository analysisMetadataRepository,
                             RestTemplate restTemplate,
                             @Value("${wordcloud.storage.path:wordclouds}") String wordCloudStoragePath) {
        this.analysisMetadataRepository = analysisMetadataRepository;
        this.restTemplate = restTemplate;
        this.wordCloudStoragePath = wordCloudStoragePath != null ? wordCloudStoragePath : "wordclouds";
        createWordCloudDirectory();
    }

    private void createWordCloudDirectory() {
        try {
            Path path = Paths.get(wordCloudStoragePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            logger.info("Word cloud directory created/verified at: {}", path.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to create word cloud directory", e);
            throw new RuntimeException("Failed to create word cloud directory", e);
        }
    }

    public AnalysisResponseDTO analyzeFile(Long fileId) {
        Optional<AnalysisMetadata> existingAnalysis = analysisMetadataRepository.findByFileId(fileId);
        if (existingAnalysis.isPresent()) {
            logger.info("Found existing analysis for file ID: {}", fileId);
            return convertToResponseDTO(existingAnalysis.get());
        }

        try {
            String fileUrl = fileStorageServiceUrl + "/files/" + fileId;
            logger.info("Fetching file content from: {}", fileUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(getFileInputStreamFromUrl(fileUrl)));
            
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
            
            try {
                String wordCloudPath = generateWordCloud(content.toString());
                metadata.setWordCloudPath(wordCloudPath);
                logger.info("Word cloud generated successfully for file ID: {}", fileId);
            } catch (Exception e) {
                logger.error("Failed to generate word cloud for file ID: {}", fileId, e);
                metadata.setWordCloudPath(null);
            }
            
            FilePlagiarismResponseDTO plagiarismResponse = restTemplate.getForObject(
                fileStorageServiceUrl + "/files/plagiarism/" + fileId,
                FilePlagiarismResponseDTO.class
            );
            
            if (plagiarismResponse != null && plagiarismResponse.getPlagiarismFileId() != null) {
                metadata.setPlagiarismFileId(plagiarismResponse.getPlagiarismFileId());
                logger.info("Plagiarism check completed for file ID: {}", fileId);
            }
            
            AnalysisMetadata savedMetadata = analysisMetadataRepository.save(metadata);
            logger.info("Analysis metadata saved for file ID: {}", fileId);
            return convertToResponseDTO(savedMetadata);
            
        } catch (IOException e) {
            logger.error("Error analyzing file ID: {}", fileId, e);
            throw new RuntimeException("Error analyzing file: " + e.getMessage(), e);
        }
    }

    private String generateWordCloud(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty text provided for word cloud generation");
            return null;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("format", "png");
        requestBody.put("width", 1000);
        requestBody.put("height", 1000);
        requestBody.put("fontScale", 15);
        requestBody.put("scale", "linear");
        requestBody.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        logger.info("Sending request to QuickChart API");
        byte[] imageBytes = restTemplate.postForObject(quickChartApiUrl, request, byte[].class);
        
        if (imageBytes == null || imageBytes.length == 0) {
            logger.error("Received empty response from QuickChart API");
            return null;
        }
        
        String fileName = System.currentTimeMillis() + ".png";
        Path filePath = Paths.get(wordCloudStoragePath, fileName);
        
        Files.write(filePath, imageBytes);
        logger.info("Word cloud image saved to: {}", filePath);
        
        return fileName;
    }

    private AnalysisResponseDTO convertToResponseDTO(AnalysisMetadata metadata) {
        AnalysisResponseDTO response = new AnalysisResponseDTO();
        response.setParagraphCount(metadata.getParagraphCount());
        response.setWordCount(metadata.getWordCount());
        response.setCharacterCount(metadata.getCharacterCount());
        response.setPlagiarismFileId(metadata.getPlagiarismFileId());
        response.setWordCloudPath(metadata.getWordCloudPath());
        return response;
    }

    /**
     * Для тестирования: этот метод можно замокать, чтобы не было реального обращения к сети.
     */
    protected InputStream getFileInputStreamFromUrl(String url) throws IOException {
        return new URL(url).openStream();
    }
}

package org.example.service;

import org.example.dto.AnalysisResponseDTO;
import org.example.dto.FilePlagiarismResponseDTO;
import org.example.model.AnalysisMetadata;
import org.example.repository.AnalysisMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileAnalysisServiceTest {

    @Mock
    private AnalysisMetadataRepository analysisMetadataRepository;

    @Mock
    private RestTemplate restTemplate;

    private FileAnalysisService fileAnalysisService;

    private AnalysisMetadata testAnalysisMetadata;
    private FilePlagiarismResponseDTO testPlagiarismResponse;

    @BeforeEach
    void setUp() {
        fileAnalysisService = spy(new FileAnalysisService(analysisMetadataRepository, restTemplate, "test-wordclouds"));
        ReflectionTestUtils.setField(fileAnalysisService, "fileStorageServiceUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(fileAnalysisService, "quickChartApiUrl", "https://quickchart.io/wordcloud");
        ReflectionTestUtils.setField(fileAnalysisService, "wordCloudStoragePath", "test-wordclouds");

        testAnalysisMetadata = new AnalysisMetadata();
        testAnalysisMetadata.setId(1L);
        testAnalysisMetadata.setFileId(1L);
        testAnalysisMetadata.setParagraphCount(2L);
        testAnalysisMetadata.setWordCount(10L);
        testAnalysisMetadata.setCharacterCount(50L);
        testAnalysisMetadata.setWordCloudPath("/test-wordclouds/test.png");
        testAnalysisMetadata.setPlagiarismFileId(2L);

        testPlagiarismResponse = new FilePlagiarismResponseDTO();
        testPlagiarismResponse.setPlagiarismFileId(2L);
    }

    @Test
    void analyzeFile_WhenAnalysisExists_ShouldReturnExistingAnalysis() {
        when(analysisMetadataRepository.findByFileId(1L)).thenReturn(Optional.of(testAnalysisMetadata));

        AnalysisResponseDTO response = fileAnalysisService.analyzeFile(1L);

        assertNotNull(response);
        assertEquals(testAnalysisMetadata.getParagraphCount(), response.getParagraphCount());
        assertEquals(testAnalysisMetadata.getWordCount(), response.getWordCount());
        assertEquals(testAnalysisMetadata.getCharacterCount(), response.getCharacterCount());
        assertEquals(testAnalysisMetadata.getWordCloudPath(), response.getWordCloudPath());
        assertEquals(testAnalysisMetadata.getPlagiarismFileId(), response.getPlagiarismFileId());

        verify(analysisMetadataRepository).findByFileId(1L);
        verifyNoMoreInteractions(analysisMetadataRepository);
    }

    @Test
    void analyzeFile_WhenNewAnalysis_ShouldCreateAndSaveAnalysis() throws Exception {
        when(analysisMetadataRepository.findByFileId(1L)).thenReturn(Optional.empty());
        when(analysisMetadataRepository.save(any(AnalysisMetadata.class))).thenReturn(testAnalysisMetadata);
        when(restTemplate.getForObject(
            eq("http://localhost:8080/files/plagiarism/1"),
            eq(FilePlagiarismResponseDTO.class)
        )).thenReturn(testPlagiarismResponse);

        // Мокаем вызов new URL(...).openStream() через spy
        doReturn(new ByteArrayInputStream("test text".getBytes()))
            .when(fileAnalysisService)
            .getFileInputStreamFromUrl(any());

        AnalysisResponseDTO response = fileAnalysisService.analyzeFile(1L);

        assertNotNull(response);
        assertEquals(testAnalysisMetadata.getParagraphCount(), response.getParagraphCount());
        assertEquals(testAnalysisMetadata.getWordCount(), response.getWordCount());
        assertEquals(testAnalysisMetadata.getCharacterCount(), response.getCharacterCount());
        assertEquals(testAnalysisMetadata.getWordCloudPath(), response.getWordCloudPath());
        assertEquals(testAnalysisMetadata.getPlagiarismFileId(), response.getPlagiarismFileId());

        verify(analysisMetadataRepository).findByFileId(1L);
        verify(analysisMetadataRepository).save(any(AnalysisMetadata.class));
        verify(restTemplate).getForObject(
            eq("http://localhost:8080/files/plagiarism/1"),
            eq(FilePlagiarismResponseDTO.class)
        );
    }
} 
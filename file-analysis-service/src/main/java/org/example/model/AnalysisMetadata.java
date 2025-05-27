package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "analysis_metadata", indexes = {
    @Index(name = "idx_analysis_metadata_file_id", columnList = "file_id")
})
@Data
public class AnalysisMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_id")
    private Long fileId;
    
    private Long plagiarismFileId;
    
    private Long paragraphCount;
    private Long wordCount;
    private Long characterCount;

    @Column(name = "word_cloud_path")
    private String wordCloudPath;
}

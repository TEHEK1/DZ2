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
    
    private Long plagiarism_file_id;
    
    private Long paragraph_count;
    private Long word_count;
    private Long character_count;
}

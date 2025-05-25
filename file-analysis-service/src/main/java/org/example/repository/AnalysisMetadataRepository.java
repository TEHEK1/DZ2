package org.example.repository;

import org.example.model.AnalysisMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisMetadataRepository extends JpaRepository<AnalysisMetadata, Long> {
    Optional<AnalysisMetadata> findByFileId(Long fileId);
}
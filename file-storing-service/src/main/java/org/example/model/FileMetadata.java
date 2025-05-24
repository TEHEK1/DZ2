package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file_metadata")
@Data
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String hash;
    private String location;
}

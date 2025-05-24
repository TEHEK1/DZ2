package org.example.dto;

import lombok.Getter;

import java.io.InputStream;

@Getter
public class FileResource {
    private final InputStream inputStream;
    private final String filename;

    public FileResource(InputStream inputStream, String filename) {
        this.inputStream = inputStream;
        this.filename = filename;
    }

}
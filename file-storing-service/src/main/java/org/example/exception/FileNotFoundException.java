package org.example.exception;

public class FileNotFoundException extends FileStorageException {

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 
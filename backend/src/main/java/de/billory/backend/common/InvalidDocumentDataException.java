package de.billory.backend.common;

public class InvalidDocumentDataException extends RuntimeException {

    public InvalidDocumentDataException(String message) {
        super(message);
    }
}
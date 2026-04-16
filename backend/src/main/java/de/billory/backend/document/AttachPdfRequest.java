package de.billory.backend.document;

import jakarta.validation.constraints.NotBlank;

public class AttachPdfRequest {

    @NotBlank
    private String sourceFilePath;

    public AttachPdfRequest() {
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }
}
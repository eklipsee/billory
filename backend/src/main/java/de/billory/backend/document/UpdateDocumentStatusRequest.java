package de.billory.backend.document;

import jakarta.validation.constraints.NotNull;

public class UpdateDocumentStatusRequest {

    @NotNull
    private DocumentStatus status;

    public UpdateDocumentStatusRequest() {
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }
}
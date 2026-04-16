package de.billory.backend.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateDocumentRequest {

    @NotNull
    private DocumentType type;

    private Boolean isHistorical;

    private String invoiceNumber;

    private DocumentStatus status;

    @NotNull
    private Integer customerId;

    @NotNull
    private String documentDate;

    private String serviceDate;

    private String validUntil;

    private String notes;

    @Valid
    @NotEmpty
    private List<CreateLineItemRequest> lineItems;

    public CreateDocumentRequest() {
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public Boolean getIsHistorical() {
        return this.isHistorical;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public void setIsHistorical(Boolean isHistorical) {
        this.isHistorical = isHistorical;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(String documentDate) {
        this.documentDate = documentDate;
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<CreateLineItemRequest> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<CreateLineItemRequest> lineItems) {
        this.lineItems = lineItems;
    }
}
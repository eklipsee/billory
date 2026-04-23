package de.billory.backend.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateHistoricalDocumentRequest {

    @NotNull
    private Integer customerId;

    @NotBlank
    private String invoiceNumber;

    @NotNull
    private DocumentStatus status;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "documentDate must be in format YYYY-MM-DD")
    private String documentDate;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "documentDate must be in format YYYY-MM-DD")
    private String serviceDate;

    private String notes;

    @Valid
    @NotEmpty
    private List<CreateLineItemRequest> lineItems;

    public CreateHistoricalDocumentRequest() {
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
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
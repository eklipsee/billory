package de.billory.backend.document;

import jakarta.validation.constraints.NotNull;

public class ConvertToInvoiceRequest {

    @NotNull
    private Integer offerId;

    public ConvertToInvoiceRequest() {
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }
}
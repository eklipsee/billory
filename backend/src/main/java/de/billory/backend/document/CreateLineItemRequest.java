package de.billory.backend.document;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateLineItemRequest {

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private Double netAmount;

    public CreateLineItemRequest() {
    }

    public String getDescription() {
        return description;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }
}
package de.billory.backend.externalinvoice;

public class ExternalInvoiceSummaryResponse {

    private Integer year;
    private Double totalGross;
    private Double totalNet;
    private Double totalTax;

    public ExternalInvoiceSummaryResponse() {
    }

    public ExternalInvoiceSummaryResponse(Integer year, Double totalGross, Double totalNet, Double totalTax) {
        this.year = year;
        this.totalGross = totalGross;
        this.totalNet = totalNet;
        this.totalTax = totalTax;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(Double totalGross) {
        this.totalGross = totalGross;
    }

    public Double getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(Double totalNet) {
        this.totalNet = totalNet;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }
}
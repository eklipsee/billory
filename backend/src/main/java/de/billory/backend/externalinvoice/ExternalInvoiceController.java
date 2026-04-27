package de.billory.backend.externalinvoice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external-invoices")
public class ExternalInvoiceController {

    private final ExternalInvoiceService externalInvoiceService;

    public ExternalInvoiceController(ExternalInvoiceService externalInvoiceService) {
        this.externalInvoiceService = externalInvoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalInvoiceResponse createExternalInvoice(@Valid @RequestBody CreateExternalInvoiceRequest request) {
        return externalInvoiceService.createExternalInvoice(request);
    }

    @GetMapping
    public java.util.List<ExternalInvoiceResponse> getByYear(@RequestParam Integer year) {
        return externalInvoiceService.getByYear(year);
    }

    @GetMapping("/summary")
    public ExternalInvoiceSummaryResponse getYearlySummary(@RequestParam Integer year) {
        return externalInvoiceService.getYearlySummary(year);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteExternalInvoice(@PathVariable Integer id) {
        externalInvoiceService.deleteExternalInvoice(id);
    }

    @PutMapping("/{id}")
    public ExternalInvoiceResponse updateExternalInvoice(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateExternalInvoiceRequest request) {
        return externalInvoiceService.updateExternalInvoice(id, request);
    }
}
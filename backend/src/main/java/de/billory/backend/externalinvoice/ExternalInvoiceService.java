package de.billory.backend.externalinvoice;

import de.billory.backend.common.NotFoundException;
import de.billory.backend.settings.Settings;
import de.billory.backend.settings.SettingsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import de.billory.backend.common.InvalidDocumentDataException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ExternalInvoiceService {

    private static final Integer SETTINGS_ID = 1;
    private static final double TAX_RATE = 19.0;

    private final ExternalInvoiceRepository externalInvoiceRepository;
    private final SettingsRepository settingsRepository;

    public ExternalInvoiceService(ExternalInvoiceRepository externalInvoiceRepository,
                                  SettingsRepository settingsRepository) {
        this.externalInvoiceRepository = externalInvoiceRepository;
        this.settingsRepository = settingsRepository;
    }

    public ExternalInvoiceResponse createExternalInvoice(CreateExternalInvoiceRequest request) {

        Settings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

                String receiptsPath = settings.getReceiptsPath();

        if (receiptsPath == null || receiptsPath.isBlank()) {
            throw new InvalidDocumentDataException("Receipts path is not configured");
        }

        Path sourcePath = Path.of(request.getSourceFilePath());

        if (!Files.exists(sourcePath)) {
            throw new NotFoundException("Source PDF file not found");
        }

        if (!request.getSourceFilePath().toLowerCase().endsWith(".pdf")) {
            throw new InvalidDocumentDataException("Only PDF files can be used for external invoices");
        }

        Path targetDir = Path.of(receiptsPath, String.valueOf(request.getYear()));

        String now = LocalDateTime.now().toString();

        String targetFileName = sourcePath.getFileName().toString();
        Path targetPath = targetDir.resolve(targetFileName);

        try {
            Files.createDirectories(targetDir);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidDocumentDataException("Failed to copy external invoice PDF");
        }

        ExternalInvoice invoice = new ExternalInvoice();

        invoice.setFilePath(targetPath.toAbsolutePath().toString());

        invoice.setYear(request.getYear());
        invoice.setDate(request.getDate());
        invoice.setDescription(request.getDescription());
        invoice.setCategory(request.getCategory());
        
        double grossAmount = request.getGrossAmount();
        double netAmount = roundToTwoDecimals(grossAmount / (1 + TAX_RATE / 100));
        double taxAmount = roundToTwoDecimals(grossAmount - netAmount);

        invoice.setGrossAmount(grossAmount);
        invoice.setNetAmount(netAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTaxRate(TAX_RATE);

        // filePath, netAmount, taxAmount, taxRate kommen im nächsten Schritt

        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);

        return toResponse(externalInvoiceRepository.save(invoice));
    }

    private ExternalInvoiceResponse toResponse(ExternalInvoice invoice) {
        ExternalInvoiceResponse response = new ExternalInvoiceResponse();

        response.setId(invoice.getId());
        response.setFilePath(invoice.getFilePath());
        response.setYear(invoice.getYear());
        response.setDate(invoice.getDate());
        response.setDescription(invoice.getDescription());
        response.setCategory(invoice.getCategory());
        response.setGrossAmount(invoice.getGrossAmount());
        response.setNetAmount(invoice.getNetAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setTaxRate(invoice.getTaxRate());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());

        return response;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public java.util.List<ExternalInvoiceResponse> getByYear(Integer year) {
        return externalInvoiceRepository.findByYearOrderByDateDesc(year)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExternalInvoiceSummaryResponse getYearlySummary(Integer year) {
        java.util.List<ExternalInvoice> invoices = externalInvoiceRepository.findByYearOrderByDateDesc(year);

        double totalGross = invoices.stream()
                .mapToDouble(ExternalInvoice::getGrossAmount)
                .sum();

        double totalNet = invoices.stream()
                .mapToDouble(ExternalInvoice::getNetAmount)
                .sum();

        double totalTax = invoices.stream()
                .mapToDouble(ExternalInvoice::getTaxAmount)
                .sum();

        return new ExternalInvoiceSummaryResponse(
                year,
                roundToTwoDecimals(totalGross),
                roundToTwoDecimals(totalNet),
                roundToTwoDecimals(totalTax)
        );
    }

    public void deleteExternalInvoice(Integer id) {
        ExternalInvoice invoice = externalInvoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("External invoice not found"));

        externalInvoiceRepository.delete(invoice);
    }

    public ExternalInvoiceResponse updateExternalInvoice(Integer id, UpdateExternalInvoiceRequest request) {
        ExternalInvoice invoice = externalInvoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("External invoice not found"));

        double grossAmount = request.getGrossAmount();
        double netAmount = roundToTwoDecimals(grossAmount / (1 + TAX_RATE / 100));
        double taxAmount = roundToTwoDecimals(grossAmount - netAmount);

        invoice.setDate(request.getDate());
        invoice.setDescription(request.getDescription());
        invoice.setCategory(request.getCategory());
        invoice.setGrossAmount(grossAmount);
        invoice.setNetAmount(netAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTaxRate(TAX_RATE);
        invoice.setUpdatedAt(java.time.LocalDateTime.now().toString());

        return toResponse(externalInvoiceRepository.save(invoice));
    }
}
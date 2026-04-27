package de.billory.backend.export;

import de.billory.backend.document.Document;
import de.billory.backend.document.DocumentRepository;
import de.billory.backend.document.DocumentStatus;
import de.billory.backend.document.DocumentType;
import de.billory.backend.externalinvoice.ExternalInvoice;
import de.billory.backend.externalinvoice.ExternalInvoiceRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

@Service
public class ExportService {

    private final ExternalInvoiceRepository externalInvoiceRepository;
    private final DocumentRepository documentRepository;

    public ExportService(ExternalInvoiceRepository externalInvoiceRepository,
                     DocumentRepository documentRepository) {
        this.externalInvoiceRepository = externalInvoiceRepository;
        this.documentRepository = documentRepository;
    }

    public String exportCsv(Integer year) {

        List<ExternalInvoice> invoices = externalInvoiceRepository.findByYearOrderByDateDesc(year);
        List<Document> documents = documentRepository.findByType(DocumentType.INVOICE);

        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("date;type;description;net;tax;gross;category\n");

        List<String> rows = new ArrayList<>();

        // --- External Invoices (EXPENSE) ---
        for (ExternalInvoice invoice : invoices) {
            rows.add(invoice.getDate() + ";"
                    + "EXPENSE" + ";"
                    + csvValue(invoice.getDescription()) + ";"
                    + formatAmount(invoice.getNetAmount()) + ";"
                    + formatAmount(invoice.getTaxAmount()) + ";"
                    + formatAmount(invoice.getGrossAmount()) + ";"
                    + csvValue(invoice.getCategory()));
        }

        // --- Documents (INCOME) ---
        for (Document document : documents) {

            if (document.getStatus() == DocumentStatus.CANCELLED) {
                continue;
            }

            if (document.getDocumentDate() == null ||
                    !document.getDocumentDate().startsWith(String.valueOf(year))) {
                continue;
            }

            rows.add(document.getDocumentDate() + ";"
                    + "INCOME" + ";"
                    + csvValue("Rechnung " + document.getInvoiceNumber()) + ";"
                    + formatAmount(document.getNetTotal()) + ";"
                    + formatAmount(document.getTaxTotal()) + ";"
                    + formatAmount(document.getGrossTotal()) + ";"
                    + "");
        }

        // --- Sortieren nach Datum (String reicht, da YYYY-MM-DD) ---
        rows.sort(Comparator.naturalOrder());

        // --- In CSV schreiben ---
        for (String row : rows) {
            csv.append(row).append("\n");
        }

        return csv.toString();
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    private String formatAmount(Double value) {
        if (value == null) {
            return "0.00";
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}
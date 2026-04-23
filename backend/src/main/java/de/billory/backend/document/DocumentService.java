package de.billory.backend.document;

import de.billory.backend.common.NotFoundException;
import de.billory.backend.customer.Customer;
import de.billory.backend.customer.CustomerRepository;
import de.billory.backend.settings.Settings;
import de.billory.backend.settings.SettingsRepository;

import org.springframework.stereotype.Service;
import de.billory.backend.common.InvalidStatusTransitionException;

import de.billory.backend.common.InvalidDocumentConversionException;
import de.billory.backend.common.InvalidDocumentDataException;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

@Service
public class DocumentService {

    private static final double TAX_RATE = 19.0;

    private final SettingsRepository settingsRepository;
    private final DocumentRepository documentRepository;
    private final LineItemRepository lineItemRepository;
    private final CustomerRepository customerRepository;

    public DocumentService(DocumentRepository documentRepository,
                       LineItemRepository lineItemRepository,
                       CustomerRepository customerRepository,
                       SettingsRepository settingsRepository) {
        this.documentRepository = documentRepository;
        this.lineItemRepository = lineItemRepository;
        this.customerRepository = customerRepository;
        this.settingsRepository = settingsRepository;
    }

    public DocumentResponse createDocument(CreateDocumentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        
        String now = LocalDateTime.now().toString();

        if (request.getType() != DocumentType.OFFER && request.getType() != DocumentType.INVOICE) {
            throw new InvalidDocumentDataException("Document type must be OFFER or INVOICE");
        }

        Document document = new Document();
        document.setType(request.getType());
        document.setIsHistorical(0);

        validateDocumentData(request.getType(), request.getValidUntil(), request.getServiceDate());


        if (request.getType() == DocumentType.INVOICE) {
            document.setInvoiceNumber(generateInvoiceNumber(request.getDocumentDate()));
        }

        document.setStatus(DocumentStatus.DRAFT);

        document.setCustomer(customer);
        document.setDocumentDate(request.getDocumentDate());
        document.setServiceDate(request.getServiceDate());
        document.setValidUntil(request.getValidUntil());
        document.setNotes(request.getNotes());
        document.setGrossTotal(0.0);
        document.setNetTotal(0.0);
        document.setTaxTotal(0.0);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        Document savedDocument = documentRepository.save(document);

        List<LineItem> savedLineItems = new ArrayList<>();
        double netTotal = 0.0;

        int position = 1;

        for (CreateLineItemRequest itemRequest : request.getLineItems()) {
            double netAmount = itemRequest.getNetAmount();
            double taxAmount = roundToTwoDecimals(netAmount * (TAX_RATE / 100));
            double grossAmount = roundToTwoDecimals(netAmount + taxAmount);

            LineItem lineItem = new LineItem();
            lineItem.setDocument(savedDocument);
            lineItem.setPosition(position);
            lineItem.setDescription(itemRequest.getDescription());
            lineItem.setGrossAmount(grossAmount);
            lineItem.setNetAmount(netAmount);
            lineItem.setTaxAmount(taxAmount);
            lineItem.setTaxRate(TAX_RATE);
            lineItem.setCreatedAt(now);

            LineItem savedLineItem = lineItemRepository.save(lineItem);
            savedLineItems.add(savedLineItem);
            
            netTotal += netAmount;

            position++;
        }

        double roundedNetTotal = roundToTwoDecimals(netTotal);
        double roundedTaxTotal = roundToTwoDecimals(roundedNetTotal * (TAX_RATE / 100));
        double roundedGrossTotal = roundToTwoDecimals(roundedNetTotal + roundedTaxTotal);

        savedDocument.setNetTotal(roundedNetTotal);
        savedDocument.setTaxTotal(roundedTaxTotal);
        savedDocument.setGrossTotal(roundedGrossTotal);

        Document updatedDocument = documentRepository.save(savedDocument);

        return toResponse(updatedDocument, savedLineItems);
    }

    public DocumentResponse createHistoricalDocument(CreateHistoricalDocumentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        String now = LocalDateTime.now().toString();

        if (request.getStatus() != DocumentStatus.OPEN && request.getStatus() != DocumentStatus.PAID) {
            throw new InvalidDocumentDataException("Historical invoices must have status OPEN or PAID");
        }

        Document document = new Document();
        document.setType(DocumentType.INVOICE);
        document.setIsHistorical(1);
        document.setInvoiceNumber(request.getInvoiceNumber());
        document.setStatus(request.getStatus());
        document.setCustomer(customer);
        document.setDocumentDate(request.getDocumentDate());
        document.setServiceDate(request.getServiceDate());
        document.setValidUntil(null);
        document.setNotes(request.getNotes());
        document.setGrossTotal(0.0);
        document.setNetTotal(0.0);
        document.setTaxTotal(0.0);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        Document savedDocument = documentRepository.save(document);

        List<LineItem> savedLineItems = new ArrayList<>();
        double netTotal = 0.0;

        int position = 1;

        for (CreateLineItemRequest itemRequest : request.getLineItems()) {
            double netAmount = itemRequest.getNetAmount();
            double taxAmount = roundToTwoDecimals(netAmount * (TAX_RATE / 100));
            double grossAmount = roundToTwoDecimals(netAmount + taxAmount);

            LineItem lineItem = new LineItem();
            lineItem.setDocument(savedDocument);
            lineItem.setPosition(position);
            lineItem.setDescription(itemRequest.getDescription());
            lineItem.setGrossAmount(grossAmount);
            lineItem.setNetAmount(netAmount);
            lineItem.setTaxAmount(taxAmount);
            lineItem.setTaxRate(TAX_RATE);
            lineItem.setCreatedAt(now);

            LineItem savedLineItem = lineItemRepository.save(lineItem);
            savedLineItems.add(savedLineItem);

            netTotal += netAmount;
            position++;
        }

        double roundedNetTotal = roundToTwoDecimals(netTotal);
        double roundedTaxTotal = roundToTwoDecimals(roundedNetTotal * (TAX_RATE / 100));
        double roundedGrossTotal = roundToTwoDecimals(roundedNetTotal + roundedTaxTotal);

        savedDocument.setNetTotal(roundedNetTotal);
        savedDocument.setTaxTotal(roundedTaxTotal);
        savedDocument.setGrossTotal(roundedGrossTotal);

        Document updatedDocument = documentRepository.save(savedDocument);

        return toResponse(updatedDocument, savedLineItems);
    }

    private DocumentResponse toResponse(Document document, List<LineItem> lineItems) {
        DocumentResponse response = new DocumentResponse();

        response.setId(document.getId());
        response.setType(document.getType());
        response.setStatus(document.getStatus());
        response.setIsHistorical(document.getIsHistorical() != null && document.getIsHistorical() == 1);
        response.setInvoiceNumber(document.getInvoiceNumber());
        response.setCustomerId(document.getCustomer().getId());
        response.setCustomerName(document.getCustomer().getName());
        response.setDocumentDate(document.getDocumentDate());
        response.setServiceDate(document.getServiceDate());
        response.setValidUntil(document.getValidUntil());
        response.setGrossTotal(document.getGrossTotal());
        response.setNetTotal(document.getNetTotal());
        response.setTaxTotal(document.getTaxTotal());
        response.setPdfPath(document.getPdfPath());
        response.setNotes(document.getNotes());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());

        if (document.getConvertedFrom() != null) {
            response.setConvertedFromId(document.getConvertedFrom().getId());
        }

        List<LineItemResponse> lineItemResponses = lineItems.stream()
                .map(this::toLineItemResponse)
                .toList();

        response.setLineItems(lineItemResponses);

        return response;
    }

    private LineItemResponse toLineItemResponse(LineItem lineItem) {
        LineItemResponse response = new LineItemResponse();

        response.setId(lineItem.getId());
        response.setPosition(lineItem.getPosition());
        response.setDescription(lineItem.getDescription());
        response.setGrossAmount(lineItem.getGrossAmount());
        response.setNetAmount(lineItem.getNetAmount());
        response.setTaxAmount(lineItem.getTaxAmount());
        response.setTaxRate(lineItem.getTaxRate());
        response.setCreatedAt(lineItem.getCreatedAt());

        return response;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public DocumentResponse getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(id);

        return toResponse(document, lineItems);
    }

    public List<DocumentResponse> getAllDocuments(DocumentType type, DocumentStatus status) {
        List<Document> documents;

        if (type != null && status != null) {
            documents = documentRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            documents = documentRepository.findByType(type);
        } else if (status != null) {
            documents = documentRepository.findByStatus(status);
        } else {
            documents = documentRepository.findAll();
        }

        return documents.stream()
                .map(document -> {
                    List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(document.getId());
                    return toResponse(document, lineItems);
                })
                .toList();
    }

    public DocumentResponse updateDocumentStatus(Integer id, UpdateDocumentStatusRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        
        DocumentType type = document.getType();
        DocumentStatus currentStatus = document.getStatus();
        DocumentStatus newStatus = request.getStatus();
        boolean isHistorical = document.getIsHistorical() != null && document.getIsHistorical() == 1;

        // --- Type-based rules ---

        if (type == DocumentType.OFFER && newStatus == DocumentStatus.PAID) {
            throw new InvalidDocumentDataException("Offers must not have PAID status");
        }

        // --- Historical rules ---

        if (isHistorical && newStatus == DocumentStatus.DRAFT) {
            throw new InvalidDocumentDataException("Historical documents must not have DRAFT status");
        }

        // --- Back-to-DRAFT rules ---

        if (type == DocumentType.OFFER
                && currentStatus != DocumentStatus.DRAFT
                && newStatus == DocumentStatus.DRAFT) {
            throw new InvalidDocumentDataException("Offers must not be moved back to DRAFT");
        }

        if (type == DocumentType.INVOICE
                && !isHistorical
                && currentStatus != DocumentStatus.DRAFT
                && newStatus == DocumentStatus.DRAFT) {
            throw new InvalidDocumentDataException("Invoices must not be moved back to DRAFT");
        }

        validateStatusTransition(document.getStatus(), request.getStatus());

        document.setStatus(request.getStatus());
        document.setUpdatedAt(LocalDateTime.now().toString());

        Document updatedDocument = documentRepository.save(document);
        List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(id);

        return toResponse(updatedDocument, lineItems);
    }

    // General status transition rules.
    // Additional business rules by document type or historical flag
    // are enforced in updateDocumentStatus(...) before this method is called.
    private void validateStatusTransition(DocumentStatus currentStatus, DocumentStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case DRAFT -> newStatus == DocumentStatus.OPEN || newStatus == DocumentStatus.CANCELLED;
            case OPEN -> newStatus == DocumentStatus.PAID || newStatus == DocumentStatus.CANCELLED;
            case PAID -> newStatus == DocumentStatus.OPEN || newStatus == DocumentStatus.CANCELLED;
            case CANCELLED -> false;
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    public DocumentResponse convertToInvoice(ConvertToInvoiceRequest request) {
        Document offer = documentRepository.findById(request.getOfferId())
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (offer.getType() != DocumentType.OFFER) {
            throw new InvalidDocumentConversionException("Only offers can be converted to invoices");
        }

        validateDocumentData(DocumentType.INVOICE, null, offer.getServiceDate());

        List<LineItem> originalLineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(offer.getId());

        String now = LocalDateTime.now().toString();

        Document invoice = new Document();
        invoice.setType(DocumentType.INVOICE);

        invoice.setIsHistorical(0);

        String invoiceDate = now.substring(0, 10);
        invoice.setInvoiceNumber(generateInvoiceNumber(invoiceDate));

        invoice.setStatus(DocumentStatus.DRAFT);
        invoice.setCustomer(offer.getCustomer());
        invoice.setDocumentDate(invoiceDate);
        invoice.setServiceDate(offer.getServiceDate());
        invoice.setValidUntil(null);
        invoice.setNotes(offer.getNotes());
        invoice.setGrossTotal(offer.getGrossTotal());
        invoice.setNetTotal(offer.getNetTotal());
        invoice.setTaxTotal(offer.getTaxTotal());
        invoice.setConvertedFrom(offer);
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);

        Document savedInvoice = documentRepository.save(invoice);

        List<LineItem> copiedLineItems = new ArrayList<>();
        int position = 1;

        for (LineItem original : originalLineItems) {
            LineItem copied = new LineItem();
            copied.setDocument(savedInvoice);
            copied.setPosition(position);
            copied.setDescription(original.getDescription());
            copied.setGrossAmount(original.getGrossAmount());
            copied.setNetAmount(original.getNetAmount());
            copied.setTaxAmount(original.getTaxAmount());
            copied.setTaxRate(original.getTaxRate());
            copied.setCreatedAt(now);

            copiedLineItems.add(lineItemRepository.save(copied));
            position++;
        }

        return toResponse(savedInvoice, copiedLineItems);
    }

    private String generateInvoiceNumber(String documentDate) {
        String yearMonth = documentDate.substring(0, 7); // z. B. 2026-04
        long countThisMonth = documentRepository.countByTypeAndDocumentDateStartingWith(DocumentType.INVOICE, yearMonth);

        long nextNumber = countThisMonth + 1;

        String month = documentDate.substring(5, 7); // MM
        String yearShort = documentDate.substring(2, 4); // JJ

        return "F" + String.format("%02d", nextNumber) + month + yearShort;
    }

    private void validateDocumentData(DocumentType type, String validUntil, String serviceDate) {

        if (type == DocumentType.OFFER && (validUntil == null || validUntil.isBlank())) {
            throw new InvalidDocumentDataException("Offers must have validUntil");
        }

        if (type == DocumentType.INVOICE && validUntil != null && !validUntil.isBlank()) {
            throw new InvalidDocumentDataException("Invoices must not have validUntil");
        }

        if (type == DocumentType.INVOICE && (serviceDate == null || serviceDate.isBlank())) {
            throw new InvalidDocumentDataException("Invoices must have serviceDate");
        }
    }

    public void updatePdfPath(Integer documentId, String pdfPath) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        document.setPdfPath(pdfPath);
        document.setUpdatedAt(LocalDateTime.now().toString());

        documentRepository.save(document);
    }

    public void markDocumentAsOpenIfDraft(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getStatus() == DocumentStatus.DRAFT) {
            document.setStatus(DocumentStatus.OPEN);
            document.setUpdatedAt(LocalDateTime.now().toString());
            documentRepository.save(document);
        }
    }

    public DocumentResponse attachPdf(Integer id, AttachPdfRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getIsHistorical() == null || document.getIsHistorical() != 1) {
            throw new InvalidDocumentDataException("PDF attachment is only allowed for historical documents");
        }

        Path sourcePath = Path.of(request.getSourceFilePath());

        if (!Files.exists(sourcePath)) {
            throw new NotFoundException("Source PDF file not found");
        }

        if (!request.getSourceFilePath().toLowerCase().endsWith(".pdf")) {
            throw new InvalidDocumentDataException("Only PDF files can be attached");
        }

        Path targetDir = buildDocumentArchiveDirectory(document);
        try {
            Files.createDirectories(targetDir);

            String targetFileName = buildDocumentPdfFileName(document);
            Path targetPath = targetDir.resolve(targetFileName);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            document.setPdfPath(targetPath.toAbsolutePath().toString());
            document.setUpdatedAt(LocalDateTime.now().toString());

            Document savedDocument = documentRepository.save(document);
            List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(id);

            return toResponse(savedDocument, lineItems);

        } catch (IOException e) {
            throw new InvalidDocumentDataException("Failed to attach PDF file");
        }
    }

    private Path buildDocumentArchiveDirectory(Document document) {
        Settings settings = settingsRepository.findById(1)
                .orElseThrow(() -> new NotFoundException("Settings not found"));

        String basePath = settings.getArchivePath();

        if (basePath == null || basePath.isBlank()) {
            basePath = "generated-pdfs";
        }

        String typeFolder = document.getType() == DocumentType.INVOICE
                ? "Rechnungen"
                : "Angebote";

        String year = "unknown";
        try {
            year = document.getDocumentDate().substring(0, 4);
        } catch (Exception ignored) {
        }

        return Path.of(basePath, typeFolder, year);
    }

    private String buildDocumentPdfFileName(Document document) {
        if (document.getType() == DocumentType.INVOICE && document.getInvoiceNumber() != null && !document.getInvoiceNumber().isBlank()) {
            return "Rechnung_" + document.getInvoiceNumber() + ".pdf";
        }

        return "Angebot_" + document.getId() + ".pdf";
    }
}
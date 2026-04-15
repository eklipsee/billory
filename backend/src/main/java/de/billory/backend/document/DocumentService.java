package de.billory.backend.document;

import de.billory.backend.common.NotFoundException;
import de.billory.backend.customer.Customer;
import de.billory.backend.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import de.billory.backend.common.InvalidStatusTransitionException;

import de.billory.backend.common.InvalidDocumentConversionException;
import de.billory.backend.common.InvalidDocumentDataException;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private static final double TAX_RATE = 19.0;

    private final DocumentRepository documentRepository;
    private final LineItemRepository lineItemRepository;
    private final CustomerRepository customerRepository;

    public DocumentService(DocumentRepository documentRepository,
                           LineItemRepository lineItemRepository,
                           CustomerRepository customerRepository) {
        this.documentRepository = documentRepository;
        this.lineItemRepository = lineItemRepository;
        this.customerRepository = customerRepository;
    }

    public DocumentResponse createDocument(CreateDocumentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (request.getType() == DocumentType.INVOICE && request.getValidUntil() != null && !request.getValidUntil().isBlank()) {
            throw new InvalidDocumentDataException("Invoices must not have validUntil");
        }
        
        String now = LocalDateTime.now().toString();

        Document document = new Document();
        document.setType(request.getType());

        validateDocumentData(request.getType(), request.getValidUntil(), request.getServiceDate());

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
        double grossTotal = 0.0;
        double netTotal = 0.0;
        double taxTotal = 0.0;

        int position = 1;

        for (CreateLineItemRequest itemRequest : request.getLineItems()) {
            double grossAmount = itemRequest.getGrossAmount();
            double netAmount = roundToTwoDecimals(grossAmount / 1.19);
            double taxAmount = roundToTwoDecimals(grossAmount - netAmount);

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

            grossTotal += grossAmount;
            netTotal += netAmount;
            taxTotal += taxAmount;

            position++;
        }

        savedDocument.setGrossTotal(roundToTwoDecimals(grossTotal));
        savedDocument.setNetTotal(roundToTwoDecimals(netTotal));
        savedDocument.setTaxTotal(roundToTwoDecimals(taxTotal));

        Document updatedDocument = documentRepository.save(savedDocument);

        return toResponse(updatedDocument, savedLineItems);
    }

    private DocumentResponse toResponse(Document document, List<LineItem> lineItems) {
        DocumentResponse response = new DocumentResponse();

        response.setId(document.getId());
        response.setType(document.getType());
        response.setStatus(document.getStatus());
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

        validateStatusTransition(document.getStatus(), request.getStatus());

        document.setStatus(request.getStatus());
        document.setUpdatedAt(LocalDateTime.now().toString());

        Document updatedDocument = documentRepository.save(document);
        List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(id);

        return toResponse(updatedDocument, lineItems);
    }

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
}
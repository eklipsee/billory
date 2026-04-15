package de.billory.backend.document;

import de.billory.backend.common.NotFoundException;
import de.billory.backend.customer.Customer;
import de.billory.backend.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import de.billory.backend.common.InvalidStatusTransitionException;

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

        String now = LocalDateTime.now().toString();

        Document document = new Document();
        document.setType(request.getType());
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
}
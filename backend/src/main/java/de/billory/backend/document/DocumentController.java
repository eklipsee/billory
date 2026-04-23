package de.billory.backend.document;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.createDocument(request);
    }

    @PostMapping("/historical")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createHistoricalDocument(@Valid @RequestBody CreateHistoricalDocumentRequest request) {
        return documentService.createHistoricalDocument(request);
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Integer id) {
        return documentService.getDocumentById(id);
    }

    @GetMapping
    public List<DocumentResponse> getAllDocuments(
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) DocumentStatus status) {
        return documentService.getAllDocuments(type, status);
    }

    @PutMapping("/{id}/status")
    public DocumentResponse updateDocumentStatus(@PathVariable Integer id,
                                                @Valid @RequestBody UpdateDocumentStatusRequest request) {
        return documentService.updateDocumentStatus(id, request);
    }

    @PutMapping("/convert-to-invoice")
    public DocumentResponse convertToInvoice(@Valid @RequestBody ConvertToInvoiceRequest request) {
        return documentService.convertToInvoice(request);
    }

    @PostMapping("/{id}/attach-pdf")
    public DocumentResponse attachPdf(@PathVariable Integer id,
                                    @Valid @RequestBody AttachPdfRequest request) {
        return documentService.attachPdf(id, request);
    }
}
package de.billory.backend.pdf;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.billory.backend.document.DocumentRepository;

import java.util.Map;

import de.billory.backend.document.Document;
import de.billory.backend.common.NotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final DocumentRepository documentRepository;
    private final PdfService pdfService;

    public PdfController(PdfService pdfService, DocumentRepository documentRepository) {
        this.pdfService = pdfService;
        this.documentRepository = documentRepository;
    }

    @GetMapping("/test")
    public Map<String, String> createTestPdf() {
        String filePath = pdfService.createSimpleTestPdf();
        return Map.of("filePath", filePath);
    }

    @GetMapping("/document/{id}")
    public Map<String, String> createDocumentPdf(@PathVariable Integer id) {
        String filePath = pdfService.createDocumentPdf(id);
        return Map.of("filePath", filePath);
    }

    @GetMapping("/document/{id}/download")
    public ResponseEntity<Resource> downloadDocumentPdf(@PathVariable Integer id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getPdfPath() == null || document.getPdfPath().isBlank()) {
            throw new NotFoundException("PDF not found for document");
        }

        Path pdfPath = Path.of(document.getPdfPath());
        Resource resource = new FileSystemResource(pdfPath);

        if (!resource.exists()) {
            throw new NotFoundException("PDF file does not exist");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfPath.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
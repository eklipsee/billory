package de.billory.backend.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    List<Document> findByType(DocumentType type);

    List<Document> findByStatus(DocumentStatus status);

    List<Document> findByTypeAndStatus(DocumentType type, DocumentStatus status);
}
package de.billory.backend.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineItemRepository extends JpaRepository<LineItem, Integer> {

    List<LineItem> findByDocumentIdOrderByPositionAsc(Integer documentId);
}
package de.billory.backend.externalinvoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalInvoiceRepository extends JpaRepository<ExternalInvoice, Integer> {

    List<ExternalInvoice> findByYearOrderByDateDesc(Integer year);
}
package de.billory.backend.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.billory.backend.common.NotFoundException;
import de.billory.backend.document.Document;
import de.billory.backend.document.DocumentRepository;
import de.billory.backend.document.DocumentService;
import de.billory.backend.document.DocumentType;
import de.billory.backend.document.LineItem;
import de.billory.backend.document.LineItemRepository;
import de.billory.backend.settings.Settings;
import de.billory.backend.settings.SettingsRepository;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    private static final Integer SETTINGS_ID = 1;
    private static final String STATIC_LOGO_PATH = "generated-pdfs/logo.png";
    private static final Locale GERMAN_LOCALE = Locale.GERMANY;

    private final SettingsRepository settingsRepository;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final LineItemRepository lineItemRepository;

    public PdfService(DocumentRepository documentRepository,
                      LineItemRepository lineItemRepository,
                      DocumentService documentService,
                      SettingsRepository settingsRepository) {
        this.documentRepository = documentRepository;
        this.lineItemRepository = lineItemRepository;
        this.documentService = documentService;
        this.settingsRepository = settingsRepository;
    }

    public String createSimpleTestPdf() {
        try {
            Path outputDir = Path.of("generated-pdfs");
            Files.createDirectories(outputDir);

            Path pdfPath = outputDir.resolve("test.pdf");

            com.lowagie.text.Document pdfDocument =
                    new com.lowagie.text.Document(PageSize.A4, 50, 50, 50, 50);

            PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfPath.toFile()));
            pdfDocument.open();

            pdfDocument.add(new Paragraph("Billory PDF Test"));
            pdfDocument.add(new Paragraph("Die PDF-Erzeugung funktioniert."));

            pdfDocument.close();
            return pdfPath.toAbsolutePath().toString();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Failed to create PDF", e);
        }
    }

    public String createDocumentPdf(Integer documentId) {
        try {
            Document documentData = documentRepository.findById(documentId)
                    .orElseThrow(() -> new NotFoundException("Document not found"));

            Settings settings = settingsRepository.findById(SETTINGS_ID)
                    .orElseThrow(() -> new NotFoundException("Settings not found"));

            List<LineItem> lineItems = lineItemRepository.findByDocumentIdOrderByPositionAsc(documentId);

            Path outputDir = buildOutputDirectory(settings, documentData);
            Files.createDirectories(outputDir);

            String fileName = buildPdfFileName(documentData);
            Path pdfPath = outputDir.resolve(fileName);

            com.lowagie.text.Document pdfDocument =
                    new com.lowagie.text.Document(PageSize.A4, 50, 50, 50, 50);

            PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfPath.toFile()));

            pdfDocument.open();

            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            addHeader(pdfDocument, settings, companyFont, normalFont, smallFont);
            addRecipientAndMetaSection(pdfDocument, documentData, sectionFont, normalFont);
            addTitle(pdfDocument, documentData, titleFont);
            addItemsTable(pdfDocument, lineItems, tableHeaderFont, normalFont);
            addTotalsBlock(pdfDocument, documentData, normalFont, totalFont);
            addNotesSection(pdfDocument, documentData, settings, normalFont);
            addFooterBankSection(pdfDocument, settings, smallFont);

            pdfDocument.close();

            documentService.updatePdfPath(documentId, pdfPath.toAbsolutePath().toString());
            documentService.markDocumentAsOpenIfDraft(documentId);


            return pdfPath.toAbsolutePath().toString();

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Failed to create document PDF", e);
        }
    }

    private void addHeader(com.lowagie.text.Document pdfDocument,
                           Settings settings,
                           Font companyFont,
                           Font normalFont,
                           Font smallFont) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(new float[]{2.2f, 3.8f});
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(24f);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(0f);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);

        Image logo = loadLogoFromResources();
        if (logo != null) {
            logoCell.addElement(logo);
        }

        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setPadding(0f);
        companyCell.setVerticalAlignment(Element.ALIGN_TOP);

        companyCell.addElement(createParagraph(settings.getCompanyName(), companyFont, 0f));
        companyCell.addElement(createParagraph(buildCompanyAddressLine(settings), normalFont, 2f));
        companyCell.addElement(createParagraph(buildContactLine(settings), normalFont, 2f));

        headerTable.addCell(logoCell);
        headerTable.addCell(companyCell);

        pdfDocument.add(headerTable);
    }

    private void addRecipientAndMetaSection(com.lowagie.text.Document pdfDocument,
                                            Document documentData,
                                            Font sectionFont,
                                            Font normalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3.5f, 2.5f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(24f);

        PdfPCell recipientCell = new PdfPCell();
        recipientCell.setBorder(Rectangle.NO_BORDER);
        recipientCell.setPadding(0f);
        recipientCell.setVerticalAlignment(Element.ALIGN_TOP);

        recipientCell.addElement(createParagraph("Rechnungsempfänger", sectionFont, 8f));
        recipientCell.addElement(createParagraph(documentData.getCustomer().getName(), normalFont, 2f));
        recipientCell.addElement(createParagraph(documentData.getCustomer().getStreet(), normalFont, 2f));
        recipientCell.addElement(createParagraph(
                safe(documentData.getCustomer().getZip()) + " " + safe(documentData.getCustomer().getCity()),
                normalFont,
                0f
        ));

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setPadding(0f);
        metaCell.setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable metaTable = new PdfPTable(new float[]{1.3f, 1.7f});
        metaTable.setWidthPercentage(100);

        addMetaRow(metaTable,
                documentData.getType() == DocumentType.INVOICE ? "Rechnungs-Nr." : "Angebots-Nr.",
                resolveDocumentNumber(documentData),
                normalFont);

        addMetaRow(metaTable, "Datum", formatDate(documentData.getDocumentDate()), normalFont);

        if (hasText(documentData.getServiceDate())) {
            addMetaRow(metaTable, "Leistungsdatum", formatDate(documentData.getServiceDate()), normalFont);
        }

        if (documentData.getType() == DocumentType.OFFER && hasText(documentData.getValidUntil())) {
            addMetaRow(metaTable, "Gültig bis", formatDate(documentData.getValidUntil()), normalFont);
        }

        metaCell.addElement(metaTable);

        table.addCell(recipientCell);
        table.addCell(metaCell);

        pdfDocument.add(table);
    }

    private void addTitle(com.lowagie.text.Document pdfDocument,
                          Document documentData,
                          Font titleFont) throws DocumentException {
        String title = documentData.getType() == DocumentType.INVOICE ? "RECHNUNG" : "ANGEBOT";
        Paragraph paragraph = createParagraph(title, titleFont, 18f);
        pdfDocument.add(paragraph);
    }

    private void addItemsTable(com.lowagie.text.Document pdfDocument,
                               List<LineItem> lineItems,
                               Font headerFont,
                               Font normalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{0.8f, 5.7f, 1.7f, 1.8f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(18f);

        table.addCell(createTableHeaderCell("Pos.", headerFont, Element.ALIGN_LEFT));
        table.addCell(createTableHeaderCell("Leistung", headerFont, Element.ALIGN_LEFT));
        table.addCell(createTableHeaderCell("Netto", headerFont, Element.ALIGN_RIGHT));
        table.addCell(createTableHeaderCell("Betrag", headerFont, Element.ALIGN_RIGHT));

        for (LineItem item : lineItems) {
            table.addCell(createTableBodyCell(String.valueOf(item.getPosition()), normalFont, Element.ALIGN_LEFT));
            table.addCell(createTableBodyCell(safe(item.getDescription()), normalFont, Element.ALIGN_LEFT));
            table.addCell(createTableBodyCell(formatCurrency(item.getNetAmount()), normalFont, Element.ALIGN_RIGHT));
            table.addCell(createTableBodyCell(formatCurrency(item.getGrossAmount()), normalFont, Element.ALIGN_RIGHT));
        }

        pdfDocument.add(table);
    }

    private void addTotalsBlock(com.lowagie.text.Document pdfDocument,
                                Document documentData,
                                Font normalFont,
                                Font totalFont) throws DocumentException {
        PdfPTable wrapper = new PdfPTable(new float[]{5f, 3f});
        wrapper.setWidthPercentage(100);
        wrapper.setSpacingAfter(18f);

        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setPadding(0f);

        PdfPCell totalsCell = new PdfPCell();
        totalsCell.setBorder(Rectangle.NO_BORDER);
        totalsCell.setPadding(0f);

        PdfPTable totalsTable = new PdfPTable(new float[]{2.4f, 1.6f});
        totalsTable.setWidthPercentage(100);

        totalsTable.addCell(createTotalsLabelCell("Nettobetrag", normalFont));
        totalsTable.addCell(createTotalsValueCell(formatCurrency(documentData.getNetTotal()), normalFont));

        totalsTable.addCell(createTotalsLabelCell("Umsatzsteuer (19 %)", normalFont));
        totalsTable.addCell(createTotalsValueCell(formatCurrency(documentData.getTaxTotal()), normalFont));

        totalsTable.addCell(createTotalsLabelCell("Gesamtbetrag", totalFont));
        totalsTable.addCell(createTotalsValueCell(formatCurrency(documentData.getGrossTotal()), totalFont));

        totalsCell.addElement(totalsTable);

        wrapper.addCell(emptyCell);
        wrapper.addCell(totalsCell);

        pdfDocument.add(wrapper);
    }

    private void addNotesSection(com.lowagie.text.Document pdfDocument,
                             Document documentData,
                             Settings settings,
                             Font normalFont) throws DocumentException {

        if (documentData.getType() == DocumentType.INVOICE) {
            pdfDocument.add(createParagraph(
                    "Bitte überweisen Sie den Gesamtbetrag ohne Abzug innerhalb von 14 Tagen.",
                    normalFont,
                    8f
            ));

            if (hasText(settings.getInvoicePrivacyNotice())) {
                pdfDocument.add(createParagraph(
                        settings.getInvoicePrivacyNotice(),
                        normalFont,
                        8f
                ));
            }
        }

        if (documentData.getType() == DocumentType.OFFER) {
            if (hasText(settings.getOfferWithdrawalNotice())) {
                pdfDocument.add(createParagraph(
                        settings.getOfferWithdrawalNotice(),
                        normalFont,
                        8f
                ));
            }
        }

        if (hasText(documentData.getNotes())) {
            pdfDocument.add(createParagraph(documentData.getNotes(), normalFont, 8f));
        }
    }

    private void addFooterBankSection(com.lowagie.text.Document pdfDocument,
                                      Settings settings,
                                      Font smallFont) throws DocumentException {
        Paragraph separator = new Paragraph(" ");
        separator.setSpacingBefore(18f);
        pdfDocument.add(separator);

        PdfPTable footerTable = new PdfPTable(new float[]{1.5f, 1.8f, 1.7f});
        footerTable.setWidthPercentage(100);

        footerTable.addCell(createFooterCell(
                settings.getCompanyName() + "\n" +
                        safe(settings.getStreet()) + "\n" +
                        safe(settings.getZip()) + " " + safe(settings.getCity()),
                smallFont
        ));

        footerTable.addCell(createFooterCell(
                buildFooterBankBlock(settings),
                smallFont
        ));

        footerTable.addCell(createFooterCell(
                buildFooterTaxContactBlock(settings),
                smallFont
        ));

        pdfDocument.add(footerTable);
    }

    private void addMetaRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2f);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPCell createTableHeaderCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }

    private PdfPCell createTableBodyCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createTotalsLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        return cell;
    }

    private PdfPCell createTotalsValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        return cell;
    }

    private PdfPCell createFooterCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorder(Rectangle.TOP);
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(0f);
        cell.setPaddingLeft(0f);
        cell.setPaddingRight(8f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private Paragraph createParagraph(String text, Font font, float spacingAfter) {
        Paragraph paragraph = new Paragraph(safe(text), font);
        paragraph.setSpacingAfter(spacingAfter);
        paragraph.setLeading(13f);
        return paragraph;
    }

    private String resolveDocumentNumber(Document documentData) {
        if (documentData.getType() == DocumentType.INVOICE) {
            return safe(documentData.getInvoiceNumber());
        }
        return String.valueOf(documentData.getId());
    }

    private String buildCompanyAddressLine(Settings settings) {
        return safe(settings.getStreet()) + ", " +
                safe(settings.getZip()) + " " +
                safe(settings.getCity());
    }

    private String buildContactLine(Settings settings) {
        StringBuilder builder = new StringBuilder();

        if (hasText(settings.getPhone())) {
            builder.append("Tel.: ").append(settings.getPhone());
        }

        if (hasText(settings.getEmail())) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append("E-Mail: ").append(settings.getEmail());
        }

        return builder.toString();
    }

    private String buildFooterBankBlock(Settings settings) {
        StringBuilder builder = new StringBuilder();

        if (hasText(settings.getBankName())) {
            builder.append(settings.getBankName());
        }

        if (hasText(settings.getIban())) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("IBAN: ").append(settings.getIban());
        }

        return builder.toString();
    }

    private Path buildOutputDirectory(Settings settings, Document documentData) {
        String basePath = settings.getArchivePath();

        if (!hasText(basePath)) {
            basePath = "generated-pdfs";
        }

        String typeFolder = documentData.getType() == DocumentType.INVOICE
                ? "Rechnungen"
                : "Angebote";

        String year = extractYear(documentData.getDocumentDate());

        return Path.of(basePath, typeFolder, year);
    }

    private String buildFooterTaxContactBlock(Settings settings) {
        StringBuilder builder = new StringBuilder();

        if (hasText(settings.getTaxNumber())) {
            builder.append("Steuer-Nr.: ").append(settings.getTaxNumber());
        }

        if (hasText(settings.getEmail())) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(settings.getEmail());
        }

        if (hasText(settings.getPhone())) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(settings.getPhone());
        }

        return builder.toString();
    }

    private String formatCurrency(Double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(GERMAN_LOCALE);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(amount == null ? 0.0 : amount) + " EUR";
    }

    private String formatDate(String rawDate) {
        if (!hasText(rawDate)) {
            return "";
        }

        try {
            return LocalDate.parse(rawDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception ignored) {
            return rawDate;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String buildPdfFileName(Document documentData) {
        if (documentData.getType() == DocumentType.INVOICE && hasText(documentData.getInvoiceNumber())) {
            return "Rechnung_" + documentData.getInvoiceNumber() + ".pdf";
        }

        return "Angebot_" + documentData.getId() + ".pdf";
    }

    private String extractYear(String date) {
        if (!hasText(date)) {
            return "unknown";
        }

        try {
            return LocalDate.parse(date).getYear() + "";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Image loadLogoFromResources() {
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("static/logo.png")) {

            if (inputStream == null) {
                return null;
            }

            byte[] imageBytes = inputStream.readAllBytes();
            Image logo = Image.getInstance(imageBytes);
            logo.scaleToFit(120f, 70f);

            return logo;

        } catch (Exception e) {
            return null;
        }
    }
}
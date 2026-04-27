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
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
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

import java.awt.Color;
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
                    new com.lowagie.text.Document(PageSize.A4, 60, 60, 50, 160);

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
                    new com.lowagie.text.Document(PageSize.A4, 50, 50, 70, 120);

            PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfPath.toFile()));
            writer.setPageEvent(new InvoicePageEvent(settings, loadLogoFromResources()));

            pdfDocument.open();

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 10f);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 8.5f);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 22f);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 11f);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 10f);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 11f);

            addTopLogoSpacer(pdfDocument);
            addInvoiceLikeTitle(pdfDocument, documentData, titleFont);
            addInvoiceLikeRecipientBlock(pdfDocument, documentData, normalFont);
            addInvoiceLikeMetaLine(pdfDocument, documentData, metaFont, smallFont);
            addInvoiceLikeItemsTable(pdfDocument, lineItems, tableHeaderFont, normalFont);
            addInvoiceLikeTotals(pdfDocument, documentData, totalFont);
            addInvoiceLikePaymentNotice(pdfDocument, documentData, settings, normalFont);

            pdfDocument.close();

            documentService.updatePdfPath(documentId, pdfPath.toAbsolutePath().toString());
            documentService.markDocumentAsOpenIfDraft(documentId);
            return pdfPath.toAbsolutePath().toString();

        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Failed to create document PDF", e);
        }
    }

    private void addTopLogoSpacer(com.lowagie.text.Document pdfDocument) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(118f);
        pdfDocument.add(spacer);
    }

    private Paragraph createParagraph(String text, Font font, float spacingAfter) {
        Paragraph paragraph = new Paragraph(safe(text), font);
        paragraph.setSpacingAfter(spacingAfter);
        paragraph.setLeading(font.getSize() + 2f);
        return paragraph;
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

    private String formatCurrency(Double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(GERMAN_LOCALE);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(amount == null ? 0.0 : amount) + " EUR";
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
            return String.valueOf(LocalDate.parse(date).getYear());
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
            return Image.getInstance(imageBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private void addInvoiceLikeTitle(com.lowagie.text.Document pdfDocument,
                                     Document documentData,
                                     Font titleFont) throws DocumentException {
        String title = documentData.getType() == DocumentType.INVOICE ? "RECHNUNG" : "ANGEBOT";
        Paragraph paragraph = createParagraph(title, titleFont, 18f);
        pdfDocument.add(paragraph);
    }

    private void addInvoiceLikeRecipientBlock(com.lowagie.text.Document pdfDocument,
                                              Document documentData,
                                              Font normalFont) throws DocumentException {
        if (documentData.getType() == DocumentType.INVOICE) {
            pdfDocument.add(createParagraph("Herr", normalFont, 0f));
        }

        pdfDocument.add(createParagraph(documentData.getCustomer().getName(), normalFont, 0f));
        pdfDocument.add(createParagraph(documentData.getCustomer().getStreet(), normalFont, 0f));
        pdfDocument.add(createParagraph(
                safe(documentData.getCustomer().getZip()) + " " + safe(documentData.getCustomer().getCity()),
                normalFont,
                20f
        ));
    }

    private void addInvoiceLikeMetaLine(com.lowagie.text.Document pdfDocument,
                                        Document documentData,
                                        Font normalFont,
                                        Font smallFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(0f);

        String leftText = documentData.getType() == DocumentType.INVOICE
                ? "Rechnungs-Nr. " + safe(documentData.getInvoiceNumber())
                : "Angebot " + documentData.getId();

        String rightText = "Datum: " + formatDateShort(documentData.getDocumentDate());

        PdfPCell leftCell = new PdfPCell(new Phrase(leftText, normalFont));
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(0f);

        PdfPCell rightCell = new PdfPCell(new Phrase(rightText, normalFont));
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(0f);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(leftCell);
        table.addCell(rightCell);
        pdfDocument.add(table);

        if (documentData.getType() == DocumentType.INVOICE) {
            pdfDocument.add(createParagraph("(Bei Zahlungen und Schriftverkehr angeben!)", smallFont, 2f));
        }

        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        separator.setSpacingAfter(16f);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderColor(Color.GRAY);
        lineCell.setFixedHeight(1f);
        lineCell.setPadding(0f);
        separator.addCell(lineCell);
        pdfDocument.add(separator);

        if (documentData.getType() == DocumentType.OFFER && hasText(documentData.getValidUntil())) {
            pdfDocument.add(createParagraph("Gültig bis: " + formatDateShort(documentData.getValidUntil()), normalFont, 10f));
        }
    }

    private void addInvoiceLikeItemsTable(com.lowagie.text.Document pdfDocument,
                                          List<LineItem> lineItems,
                                          Font headerFont,
                                          Font normalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{0.9f, 5.9f, 1.4f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(22f);

        table.addCell(createHeaderCell("Pos", headerFont, Element.ALIGN_LEFT));
        table.addCell(createHeaderCell("Leistung", headerFont, Element.ALIGN_LEFT));
        table.addCell(createHeaderCell("Preis", headerFont, Element.ALIGN_LEFT));

        for (LineItem item : lineItems) {
            table.addCell(createBodyCell(String.valueOf(item.getPosition()), normalFont, Element.ALIGN_LEFT));
            table.addCell(createBodyCell(safe(item.getDescription()), normalFont, Element.ALIGN_LEFT));
            table.addCell(createBodyCell(formatCurrency(item.getNetAmount()), normalFont, Element.ALIGN_RIGHT));
        }

        pdfDocument.add(table);
    }

    private void addInvoiceLikeTotals(com.lowagie.text.Document pdfDocument,
                                      Document documentData,
                                      Font totalFont) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(45f);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingAfter(34f);

        table.addCell(createTotalsCell("Nettobetrag", totalFont, Element.ALIGN_LEFT));
        table.addCell(createTotalsCell(formatCurrency(documentData.getNetTotal()), totalFont, Element.ALIGN_RIGHT));

        table.addCell(createTotalsCell("zzgl. 19 % USt.", totalFont, Element.ALIGN_LEFT));
        table.addCell(createTotalsCell(formatCurrency(documentData.getTaxTotal()), totalFont, Element.ALIGN_RIGHT));

        table.addCell(createTotalsCell("Gesamtbetrag", totalFont, Element.ALIGN_LEFT));
        table.addCell(createTotalsCell(formatCurrency(documentData.getGrossTotal()), totalFont, Element.ALIGN_RIGHT));

        pdfDocument.add(table);
    }

    private void addInvoiceLikePaymentNotice(com.lowagie.text.Document pdfDocument,
                                             Document documentData,
                                             Settings settings,
                                             Font normalFont) throws DocumentException {
        PdfPTable topLine = new PdfPTable(1);
        topLine.setWidthPercentage(100);
        topLine.setSpacingAfter(22f);
        PdfPCell topLineCell = new PdfPCell();
        topLineCell.setBorder(Rectangle.BOTTOM);
        topLineCell.setBorderColor(Color.GRAY);
        topLineCell.setFixedHeight(1f);
        topLineCell.setPadding(0f);
        topLine.addCell(topLineCell);
        pdfDocument.add(topLine);

        if (documentData.getType() == DocumentType.INVOICE) {
            pdfDocument.add(createParagraph(
                    "Der Gesamtbetrag ist nach Erhalt\nsofort und ohne Abzug zahlbar!",
                    normalFont,
                    0f
            ));

            if (hasText(settings.getInvoicePrivacyNotice())) {
                String cleanedNotice = settings.getInvoicePrivacyNotice()
                        .replace("aus den setting:", "")
                        .trim();

                Paragraph notice = createParagraph(cleanedNotice, normalFont, 0f);
                notice.setSpacingBefore(12f);
                pdfDocument.add(notice);
            }
        }

        if (documentData.getType() == DocumentType.OFFER && hasText(settings.getOfferWithdrawalNotice())) {
            pdfDocument.add(createParagraph(settings.getOfferWithdrawalNotice(), normalFont, 0f));
        }
    }

    private PdfPCell createHeaderCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(150, 150, 150));
        cell.setBorder(Rectangle.BOX);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createBodyCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorder(Rectangle.BOX);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(6f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private PdfPCell createTotalsCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(1f);
        cell.setPaddingBottom(1f);
        cell.setHorizontalAlignment(alignment);
        cell.setNoWrap(true); // <-- DAS FEHLT
        return cell;
    }

    private String formatDateShort(String rawDate) {
        if (!hasText(rawDate)) {
            return "";
        }

        try {
            return LocalDate.parse(rawDate).format(DateTimeFormatter.ofPattern("d.M.yyyy"));
        } catch (Exception ignored) {
            return rawDate;
        }
    }

    private static class InvoicePageEvent extends PdfPageEventHelper {
        private final Settings settings;
        private final Image logoTemplate;

        private InvoicePageEvent(Settings settings, Image logoTemplate) {
            this.settings = settings;
            this.logoTemplate = logoTemplate;
        }

        @Override
        public void onEndPage(PdfWriter writer, com.lowagie.text.Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                float left = document.left();
                float right = document.right();
                float pageTop = document.getPageSize().getHeight();

                drawTopLogo(cb, left, right, pageTop);
                drawFooter(cb, left, right);
            } catch (Exception ignored) {
            }
        }

        private void drawTopLogo(PdfContentByte cb, float left, float right, float pageTop) throws Exception {
            if (logoTemplate == null) {
                return;
            }

            Image logo = Image.getInstance(logoTemplate);
            logo.scaleToFit(300f, 130f);
            float x = left + ((right - left) - logo.getScaledWidth()) / 2f;
            float y = pageTop - 165f;
            logo.setAbsolutePosition(x, y);
            cb.addImage(logo);
        }

        private void drawFooter(PdfContentByte cb, float left, float right) throws DocumentException {
            float line1Y = 110f;

            cb.saveState();
            cb.setColorStroke(Color.GRAY);
            cb.moveTo(left, line1Y);
            cb.lineTo(right, line1Y);
            cb.stroke();
            cb.restoreState();

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 9f);

            float footerTopY = 102f;
            float footerBottomY = 15f;
            float totalWidth = right - left;

            float col1Left = left;
            float col1Right = left + totalWidth * 0.32f;

            float col2Left = left + totalWidth * 0.36f;
            float col2Right = left + totalWidth * 0.62f;

            float col3Left = left + totalWidth * 0.68f;
            float col3Right = right;

            ColumnText ct1 = new ColumnText(cb);
            ct1.setSimpleColumn(col1Left, footerBottomY, col1Right, footerTopY);
            ct1.addText(new Phrase(buildFooterLeft(), footerFont));
            ct1.go();

            ColumnText ct2 = new ColumnText(cb);
            ct2.setSimpleColumn(col2Left, footerBottomY, col2Right, footerTopY);
            ct2.addText(new Phrase(buildFooterCenter(), footerFont));
            ct2.go();

            ColumnText ct3 = new ColumnText(cb);
            ct3.setSimpleColumn(col3Left, footerBottomY, col3Right, footerTopY);
            ct3.addText(new Phrase(buildFooterBottom(), footerFont));
            ct3.go();
        }

        private String buildFooterLeft() {
            StringBuilder builder = new StringBuilder();
            builder.append(safe(settings.getCompanyName()));
            if (hasText(settings.getOwnerName())) {
                builder.append("\nInh. ").append(settings.getOwnerName());
            }
            if (hasText(settings.getStreet())) {
                builder.append("\n").append(settings.getStreet());
            }
            if (hasText(settings.getZip()) || hasText(settings.getCity())) {
                builder.append("\n").append(safe(settings.getZip())).append(" ").append(safe(settings.getCity()));
            }
            return builder.toString().trim();
        }

        private String buildFooterCenter() {
            StringBuilder builder = new StringBuilder();
            if (hasText(settings.getPhone())) {
                builder.append("Tel.: ").append(settings.getPhone());
            }
            if (hasText(settings.getEmail())) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append("E-Mail: ").append(settings.getEmail());
            }
            return builder.toString();
        }

        private String buildFooterBottom() {
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
            if (hasText(settings.getTaxNumber())) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append("USt-Nr.: ").append(settings.getTaxNumber());
            }
            return builder.toString();
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }
    }
}

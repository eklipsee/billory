# Architekturdiagramme
## Rechnungs- und Angebotssoftware – Baum Performance Stahl

**Version:** 2.0  
**Stand:** April 2026  

---

## Inhaltsverzeichnis

1. [Systemarchitektur – Gesamtübersicht](#1-systemarchitektur--gesamtübersicht)
2. [Datenfluss – Rechnung erstellen](#2-datenfluss--rechnung-erstellen)
3. [Datenfluss – Angebot zu Rechnung konvertieren](#3-datenfluss--angebot-zu-rechnung-konvertieren)
4. [Datenfluss – Historische Rechnung übernehmen](#4-datenfluss--historische-rechnung-übernehmen)
5. [Datenfluss – Vorhandenes PDF an historisches Dokument anhängen](#5-datenfluss--vorhandenes-pdf-an-historisches-dokument-anhängen)
6. [Datenfluss – Externe Belege (geplant)](#6-datenfluss--externe-belege-geplant)

---

## 1 Systemarchitektur – Gesamtübersicht

Das Diagramm zeigt die aktuelle Systemarchitektur der Anwendung auf Basis von React, Electron und einem Spring-Boot-Backend.

```mermaid
graph TB
    subgraph OS["Windows 10 / 11"]

        subgraph Desktop["Electron Desktop-Anwendung"]
            UI["React Frontend"]
        end

        subgraph Backend["Spring Boot Backend"]
            Controllers["Controller Layer"]
            Services["Service Layer"]
            Repositories["Repository Layer"]
            PdfService["PDF-Service"]
        end

        subgraph Storage["Lokaler Speicher"]
            DB[("SQLite / billory.db")]
            Archive["Archiv\n<archivePath>"]
            Receipts["Belege\n<receiptsPath>"]
            Resources["Resources\nlogo.png"]
        end
    end

    UI --> Controllers
    Controllers --> Services
    Services --> Repositories
    Services --> PdfService
    Repositories --> DB
    PdfService --> Archive
    Services --> Receipts
    PdfService --> Resources
```

### Erläuterung der Schichten

| Schicht | Technologie | Aufgabe |
|---|---|---|
| Frontend | React + TypeScript | Benutzeroberfläche, Formulare, Tabellen |
| Desktop-Hülle | Electron | Lokale Desktop-Anwendung |
| Backend API | Spring Boot (Java 21) | REST-API, Validierung, Geschäftslogik |
| Service Layer | Spring Services | Berechnungen, Nummerierung, Statuslogik, PDF-Handling |
| Repository Layer | Spring Data JPA / Hibernate | Datenbankzugriffe auf SQLite |
| Datenbank | SQLite | Lokale Speicherung aller Anwendungsdaten |
| PDF-Service | OpenPDF | Erzeugung von Angebots- und Rechnungs-PDFs |
| Migrationen | Flyway | Versionierung und Aufbau des Schemas |
| Dateisystem | Java NIO | Archivierung erzeugter und verknüpfter PDFs |

---

## 2 Datenfluss – Rechnung erstellen

Vom Speichern einer normalen Rechnung bis zur PDF-Erzeugung.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React / Electron
    participant API as Spring Boot Controller
    participant DS as DocumentService
    participant DR as DocumentRepository
    participant LR as LineItemRepository
    participant DB as SQLite
    participant PS as PdfService
    participant FS as Dateisystem

    User->>UI: Füllt Formular aus\n(Kunde, Positionen, Datum)
    UI->>API: POST /api/documents

    API->>DS: createDocument(request)
    DS->>DS: Validierung der Eingabedaten
    DS->>DS: Nettobeträge je Position verarbeiten
    DS->>DS: Steuer = Netto * 0,19
    DS->>DS: Brutto = Netto + Steuer
    DS->>DS: Rechnungsnummer automatisch erzeugen
    DS->>DS: Status = DRAFT

    DS->>DR: save(document)
    DR->>DB: INSERT INTO documents

    loop Für jede Position
        DS->>LR: save(lineItem)
        LR->>DB: INSERT INTO line_items
    end

    DS->>DR: save(updatedDocumentTotals)
    DR->>DB: UPDATE documents SET totals

    DS-->>API: DocumentResponse
    API-->>UI: Dokument angelegt

    User->>UI: PDF erzeugen
    UI->>API: GET /api/pdf/document/{id}
    API->>PS: createDocumentPdf(id)

    PS->>DR: Dokument laden
    DR->>DB: SELECT document
    PS->>LR: Positionen laden
    LR->>DB: SELECT line_items

    PS->>PS: PDF aufbauen
    PS->>FS: PDF im Archiv speichern
    PS->>DS: pdfPath aktualisieren
    PS->>DS: Status DRAFT -> OPEN

    API-->>UI: filePath
```

### Fachliche Regeln

- Normale Rechnungen erhalten ihre Rechnungsnummer automatisch.
- Neue Rechnungen starten immer mit Status `DRAFT`.
- Erst bei erfolgreicher PDF-Erzeugung wird ein `DRAFT` automatisch zu `OPEN`.
- Positionswerte werden als **Nettobeträge** eingegeben.
- Umsatzsteuer und Bruttobetrag werden automatisch berechnet.

---

## 3 Datenfluss – Angebot zu Rechnung konvertieren

Ein bestehendes Angebot wird in eine neue Rechnung überführt.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React / Electron
    participant API as Spring Boot Controller
    participant DS as DocumentService
    participant DR as DocumentRepository
    participant LR as LineItemRepository
    participant DB as SQLite
    participant PS as PdfService

    User->>UI: Öffnet Angebot
    User->>UI: Klickt „In Rechnung umwandeln"
    UI->>API: PUT /api/documents/convert-to-invoice

    API->>DS: convertToInvoice(request)
    DS->>DR: Angebot laden
    DR->>DB: SELECT document
    DS->>LR: Angebotspositionen laden
    LR->>DB: SELECT line_items

    DS->>DS: Prüfen: Typ = OFFER
    DS->>DS: Neue Rechnungsnummer erzeugen
    DS->>DS: Neue Rechnung anlegen
    DS->>DS: isHistorical = false
    DS->>DS: Status = DRAFT

    DS->>DR: save(invoice)
    DR->>DB: INSERT INTO documents

    loop Positionen kopieren
        DS->>LR: save(copiedLineItem)
        LR->>DB: INSERT INTO line_items
    end

    DS-->>API: DocumentResponse
    API-->>UI: Rechnung angelegt

    User->>UI: PDF erzeugen
    UI->>API: GET /api/pdf/document/{id}
    API->>PS: createDocumentPdf(id)
    PS-->>API: filePath
    API-->>UI: Rechnung erzeugt
```

### Fachliche Regeln

- Nur Dokumente vom Typ `OFFER` dürfen konvertiert werden.
- Die neue Rechnung erhält eine neue automatische Rechnungsnummer.
- Die konvertierte Rechnung ist **kein** historisches Dokument.
- Die Positionsdaten werden aus dem Angebot übernommen.

---

## 4 Datenfluss – Historische Rechnung übernehmen

Bereits existierende Kundenrechnungen können manuell nachträglich ins System übernommen werden.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React / Electron
    participant API as Spring Boot Controller
    participant DS as DocumentService
    participant DR as DocumentRepository
    participant LR as LineItemRepository
    participant DB as SQLite

    User->>UI: Wählt „Historische Rechnung anlegen"
    User->>UI: Gibt ein:\n- Kunde\n- Rechnungsnummer\n- Rechnungsdatum\n- Leistungsdatum\n- Status\n- Positionen

    UI->>API: POST /api/documents

    API->>DS: createDocument(request)
    DS->>DS: Validierung
    DS->>DS: isHistorical = true
    DS->>DS: Rechnungsnummer aus Request übernehmen
    DS->>DS: Status aus Request übernehmen
    DS->>DS: Verbotene Statuswerte prüfen

    DS->>DR: save(document)
    DR->>DB: INSERT INTO documents

    loop Für jede Position
        DS->>LR: save(lineItem)
        LR->>DB: INSERT INTO line_items
    end

    DS->>DR: save(updatedDocumentTotals)
    DR->>DB: UPDATE documents SET totals

    DS-->>API: DocumentResponse
    API-->>UI: Historische Rechnung angelegt
```

### Fachliche Regeln

- Historische Rechnungen verwenden eine **manuell vorgegebene Rechnungsnummer**.
- Historische Rechnungen sind keine Entwürfe.
- Für historische Rechnungen ist `DRAFT` nicht erlaubt.
- Historische Rechnungen dürfen direkt z. B. als `OPEN` oder `PAID` angelegt werden.
- Historische Rechnungen zählen nicht zur automatischen neuen Rechnungsnummernvergabe.

---

## 5 Datenfluss – Vorhandenes PDF an historisches Dokument anhängen

Ein bereits vorhandenes PDF wird einem historischen Dokument zugeordnet und ins Archiv kopiert.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React / Electron
    participant API as Spring Boot Controller
    participant DS as DocumentService
    participant DR as DocumentRepository
    participant DB as SQLite
    participant FS as Dateisystem

    User->>UI: Wählt historische Rechnung
    User->>UI: Wählt vorhandene PDF-Datei
    UI->>API: POST /api/documents/{id}/attach-pdf\n{ sourceFilePath }

    API->>DS: attachPdf(id, request)
    DS->>DR: Dokument laden
    DR->>DB: SELECT document

    DS->>DS: Prüfen: Dokument existiert?
    DS->>DS: Prüfen: Dokument ist historisch?
    DS->>DS: Prüfen: Quelldatei existiert?
    DS->>DS: Prüfen: Dateiendung = .pdf

    DS->>FS: Zielordner im Archiv erzeugen
    DS->>FS: PDF ins Archiv kopieren
    DS->>DS: pdfPath auf neuen Archivpfad setzen

    DS->>DR: save(document)
    DR->>DB: UPDATE documents SET pdf_path

    DS-->>API: aktualisierte DocumentResponse
    API-->>UI: PDF erfolgreich verknüpft
```

### Fachliche Regeln

- Das Anhängen eines vorhandenen PDFs ist **nur** bei historischen Dokumenten erlaubt.
- Die Quelldatei wird nicht nur referenziert, sondern in das eigene Archiv kopiert.
- Gespeichert wird der neue Archivpfad im Feld `pdfPath`.
- Das Original kann z. B. aus Downloads, Desktop oder einem alten Ordner stammen.

---

## 6 Datenfluss – Externe Belege (geplant)

> Hinweis: Dieser Ablauf ist fachlich vorgesehen, im aktuellen Backend-Stand jedoch noch nicht umgesetzt.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React / Electron
    participant API as Spring Boot Controller
    participant EIS as ExternalInvoiceService
    participant EIR as ExternalInvoiceRepository
    participant DB as SQLite
    participant FS as Dateisystem

    User->>UI: Wählt Jahr (z. B. 2026)
    UI->>API: GET /api/external-invoices/year/2026

    API->>EIS: Lade Belege für Jahr
    EIS->>FS: Lese Ordner <receiptsPath>/2026/
    FS-->>EIS: PDF-Dateien
    EIS->>EIR: Lade bereits erfasste Belege
    EIR->>DB: SELECT external_invoices
    DB-->>EIR: Metadaten
    EIR-->>EIS: bekannte Belege
    EIS-->>API: Übersicht
    API-->>UI: Liste der Belege

    User->>UI: Öffnet PDF
    User->>UI: Gibt Datum, Beschreibung, Kategorie und Nettobetrag / Bruttobetrag ein
    UI->>API: POST /api/external-invoices

    API->>EIS: create(request)
    EIS->>EIS: Steuerberechnung
    EIS->>EIR: save(externalInvoice)
    EIR->>DB: INSERT INTO external_invoices

    API-->>UI: Beleg gespeichert
```

### Ziel des Moduls

- Externe Eingangsrechnungen und Belege verwalten
- PDFs innerhalb der Anwendung anzeigen
- Beträge für steuerliche Auswertung erfassen
- Jahresübersichten und Exporte bereitstellen

---

*Version 2.0 · Stand: April 2026 · Baum Performance Stahl*

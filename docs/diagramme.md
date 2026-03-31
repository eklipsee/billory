# Architekturdiagramme
## Rechnungs- und Angebotssoftware – Baum Performance Stahl

**Version:** 1.0  
**Stand:** März 2025  

---

## Inhaltsverzeichnis

1. [Systemarchitektur – Gesamtübersicht](#1-systemarchitektur--gesamtübersicht)
2. [Datenfluss – Rechnung erstellen](#2-datenfluss--rechnung-erstellen)
3. [Datenfluss – Angebot zu Rechnung konvertieren](#3-datenfluss--angebot-zu-rechnung-konvertieren)
4. [Datenfluss – Externen Beleg erfassen](#4-datenfluss--externen-beleg-erfassen)

---

## 1 Systemarchitektur – Gesamtübersicht

Das Diagramm zeigt alle Schichten der Anwendung und wie sie miteinander kommunizieren.

```mermaid
graph TB
    subgraph OS["Windows 10 / 11"]

        subgraph Electron["Electron-Anwendung"]

            subgraph Renderer["Renderer Process (React)"]
                UI_Pages["Seiten\nKunden · Angebote\nRechnungen · Belege"]
                UI_Components["Komponenten\nFormulare · Tabellen\nPDF-Viewer"]
                UI_State["State Management\n(React Context)"]
            end

            Preload["Preload Script\n(Sicherheitsbrücke)\ncontextBridge"]

            subgraph Main["Main Process (Node.js)"]
                IPC["IPC Handler\nipcMain.handle()"]

                subgraph Services["Service Layer"]
                    CustomerService["CustomerService"]
                    DocumentService["DocumentService"]
                    PdfService["PdfService"]
                    ExportService["ExportService"]
                    AuthService["AuthService"]
                    ExternalInvoiceService["ExternalInvoiceService"]
                end

                subgraph Repositories["Repository Layer"]
                    CustomerRepo["CustomerRepository"]
                    DocumentRepo["DocumentRepository"]
                    ExternalInvoiceRepo["ExternalInvoiceRepository"]
                    SettingsRepo["SettingsRepository"]
                end
            end
        end

        subgraph Storage["Lokaler Speicher"]
            DB[("SQLite\ndata.db")]
            PDFArchive["PDF-Archiv\n/archive/"]
            Receipts["Belege-Ordner\n/belege/YYYY/"]
            Backup["Backup\n/backup/"]
        end
    end

    UI_Pages <--> UI_State
    UI_Components <--> UI_State
    UI_State -- "window.api.invoke()" --> Preload
    Preload -- "ipcRenderer.invoke()" --> IPC
    IPC --> Services
    Services --> Repositories
    Repositories <--> DB
    PdfService --> PDFArchive
    ExternalInvoiceService --> Receipts
    Services --> Backup
```

### Erläuterung der Schichten

| Schicht | Technologie | Aufgabe |
|---|---|---|
| **Renderer Process** | React + TypeScript | Benutzeroberfläche, Formulare, Ansichten |
| **Preload Script** | Electron contextBridge | Sicherheitsschicht zwischen UI und Backend |
| **IPC Handler** | Electron ipcMain | Empfängt Aufrufe vom Frontend, leitet weiter |
| **Service Layer** | Node.js / TypeScript | Geschäftslogik, Berechnungen, Validierung |
| **Repository Layer** | better-sqlite3 | Datenbankzugriffe, SQL-Abfragen |
| **SQLite** | SQLite 3 | Lokale Datenspeicherung |
| **Dateisystem** | Node.js fs | PDFs, Belege, Backups |

---

## 2 Datenfluss – Rechnung erstellen

Vom Klick auf „Speichern" bis zum fertigen PDF auf der Festplatte.

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React (Renderer)
    participant IPC as IPC Handler (Main)
    participant DS as DocumentService
    participant PS as PdfService
    participant DR as DocumentRepository
    participant DB as SQLite
    participant FS as Dateisystem

    User->>UI: Füllt Formular aus\n(Kunde, Positionen, Datum)
    User->>UI: Klickt „Speichern & PDF erstellen"

    UI->>UI: Validierung im Frontend\n(Pflichtfelder prüfen)

    UI->>IPC: documents:create\n{ customerId, lineItems, ... }

    IPC->>DS: createDocument(data)

    DS->>DS: Berechnung der Beträge\nNetto = Brutto ÷ 1.19\nMwSt = Brutto - Netto

    DS->>DS: Rechnungsnummer generieren\n(Abfrage: wie viele\nRechnungen diesen Monat?)

    DS->>DR: save(document, lineItems)
    DR->>DB: BEGIN TRANSACTION
    DR->>DB: INSERT INTO documents
    DR->>DB: INSERT INTO line_items (n×)
    DR->>DB: UPDATE documents SET status = 'open'
    DR->>DB: COMMIT
    DB-->>DR: document.id
    DR-->>DS: document.id

    DS->>PS: generatePdf(document.id)
    PS->>DR: getById(document.id)
    DR->>DB: SELECT documents + line_items + customer
    DB-->>DR: Dokument mit allen Daten
    DR-->>PS: DocumentDetail

    PS->>PS: PDF-Layout aufbauen\n(Logo, Kopfzeile, Positionen,\nSummen, Pflichtangaben)
    PS->>FS: PDF speichern\n/archive/2025/F020325.pdf
    FS-->>PS: filePath

    PS->>DR: updatePdfPath(id, filePath)
    DR->>DB: UPDATE documents SET pdf_path
    DB-->>DR: ok
    DR-->>PS: ok
    PS-->>DS: filePath
    DS-->>IPC: { ok: true, data: { id, invoiceNumber, filePath } }
    IPC-->>UI: ApiResponse

    UI->>UI: Zeigt Erfolgsmeldung\nund öffnet PDF-Vorschau
    UI->>User: ✅ Rechnung gespeichert
```

---

## 3 Datenfluss – Angebot zu Rechnung konvertieren

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React (Renderer)
    participant IPC as IPC Handler (Main)
    participant DS as DocumentService
    participant PS as PdfService
    participant DR as DocumentRepository
    participant DB as SQLite

    User->>UI: Öffnet Angebot
    User->>UI: Klickt „In Rechnung umwandeln"
    UI->>UI: Bestätigungsdialog anzeigen
    User->>UI: Bestätigt

    UI->>IPC: documents:convertToInvoice\n{ offerId }

    IPC->>DS: convertToInvoice(offerId)

    DS->>DR: getById(offerId)
    DR->>DB: SELECT document WHERE id = offerId
    DB-->>DR: Angebotsdaten + Positionen
    DR-->>DS: DocumentDetail

    DS->>DS: Prüfen: type === 'offer'?\nStatus !== 'cancelled'?

    DS->>DS: Neue Rechnungsnummer generieren

    DS->>DR: createInvoiceFromOffer(offer, invoiceNumber)
    DR->>DB: BEGIN TRANSACTION
    DR->>DB: INSERT INTO documents\n(type='invoice', converted_from_id=offerId)
    DR->>DB: INSERT INTO line_items\n(Positionen kopieren)
    DR->>DB: UPDATE documents SET status='cancelled'\nWHERE id = offerId
    DR->>DB: COMMIT
    DB-->>DR: newInvoice.id
    DR-->>DS: newInvoice.id

    DS->>PS: generatePdf(newInvoice.id)
    PS-->>DS: filePath

    DS-->>IPC: { ok: true, data: { id, invoiceNumber } }
    IPC-->>UI: ApiResponse

    UI->>User: ✅ Rechnung F020325 erstellt
```

---

## 4 Datenfluss – Externen Beleg erfassen

```mermaid
sequenceDiagram
    actor User as Benutzer
    participant UI as React (Renderer)
    participant IPC as IPC Handler (Main)
    participant EIS as ExternalInvoiceService
    participant EIR as ExternalInvoiceRepository
    participant DB as SQLite
    participant FS as Dateisystem

    User->>UI: Wählt Jahr (z. B. 2025)

    UI->>IPC: externalInvoices:scanFolder\n{ year: 2025 }
    IPC->>EIS: scanFolder(2025)
    EIS->>FS: Lese Ordner\n/belege/2025/*.pdf
    FS-->>EIS: [datei1.pdf, datei2.pdf, ...]
    EIS->>EIR: getFilePathsByYear(2025)
    EIR->>DB: SELECT file_path WHERE year = 2025
    DB-->>EIR: bereits erfasste Pfade
    EIR-->>EIS: savedPaths[]
    EIS->>EIS: Markiere bereits erfasste Dateien
    EIS-->>IPC: [{ fileName, filePath, alreadySaved }]
    IPC-->>UI: Dateiliste

    UI->>User: Zeigt Liste aller PDFs\n✅ = bereits erfasst

    User->>UI: Klickt auf nicht erfasste PDF
    UI->>IPC: pdf:open { filePath }
    IPC-->>UI: PDF öffnet sich in Vorschau

    User->>UI: Gibt ein:\n- Datum\n- Beschreibung (z. B. „Tanken")\n- Kategorie (z. B. „Fahrtkosten")\n- Bruttobetrag (z. B. 85,00 €)

    UI->>UI: Berechnet live:\nNetto = 85,00 ÷ 1,19 = 71,43 €\nMwSt = 85,00 - 71,43 = 13,57 €

    User->>UI: Klickt „Speichern"

    UI->>IPC: externalInvoices:create\n{ filePath, year, date,\ndescription, category, grossAmount }

    IPC->>EIS: create(data)
    EIS->>EIS: Netto und MwSt berechnen\nund runden
    EIS->>EIR: save(externalInvoice)
    EIR->>DB: INSERT INTO external_invoices
    DB-->>EIR: id
    EIR-->>EIS: id
    EIS-->>IPC: { ok: true, data: { id } }
    IPC-->>UI: ApiResponse

    UI->>User: ✅ Beleg erfasst\nDatei in Liste als ✅ markiert
```

---

*Version 1.0 · Stand: März 2025 · Baum Performance Stahl*

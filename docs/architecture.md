# Datenbankarchitektur
## Rechnungs- und Angebotssoftware – Baum Performance Stahl

**Version:** 1.0  
**Stand:** März 2025  
**Datenbank:** SQLite 3  
**Speicherort:** `%APPDATA%\BaumPerformanceStahl\data.db`  

---

## Inhaltsverzeichnis

1. [Übersicht](#1-übersicht)
2. [Tabellen](#2-tabellen)
   - [settings](#21-settings)
   - [customers](#22-customers)
   - [documents](#23-documents)
   - [line_items](#24-line_items)
   - [external_invoices](#25-external_invoices)
3. [Beziehungen](#3-beziehungen)
4. [Berechnungsregeln](#4-berechnungsregeln)
5. [Indexe](#5-indexe)
6. [Migrationen](#6-migrationen)
7. [Designentscheidungen](#7-designentscheidungen)

---

## 1 Übersicht

Die Anwendung verwendet eine einzelne **SQLite-Datenbankdatei**. SQLite wurde gewählt, da die Anwendung ausschließlich lokal auf einem einzelnen Gerät betrieben wird und keine Netzwerkverbindung oder Mehrbenutzer-Unterstützung benötigt.

### Tabellen im Überblick

| Tabelle | Beschreibung | Zeilen (ca.) |
|---|---|---|
| `settings` | Anwendungskonfiguration und Firmenstammdaten | 1 (Singleton) |
| `customers` | Kundenstammdaten | mittel (10–500) |
| `documents` | Angebote und Rechnungen | mittel (10–1.000) |
| `line_items` | Positionen je Dokument | mittel (20–5.000) |
| `external_invoices` | Externe Belege (Betriebsausgaben) | mittel (50–2.000) |

### Entity-Relationship-Diagramm (vereinfacht)

```
settings (1)
    
customers (1) ──────< documents (n)
                           │
                           └──────< line_items (n)

external_invoices  (unabhängig, kein FK)
```

---

## 2 Tabellen

---

### 2.1 `settings`

Speichert die Anwendungskonfiguration und Firmenstammdaten. Enthält immer genau **einen Datensatz** (Singleton-Tabelle).

| Spalte | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | `INTEGER` | ✅ | Primärschlüssel, immer `1` |
| `company_name` | `TEXT` | ✅ | Firmenname (z. B. „Baum Performance Stahl") |
| `owner_name` | `TEXT` | ✅ | Inhabername (z. B. „Jonas Stahl") |
| `street` | `TEXT` | ✅ | Straße und Hausnummer |
| `zip` | `TEXT` | ✅ | Postleitzahl |
| `city` | `TEXT` | ✅ | Ort |
| `phone` | `TEXT` | ❌ | Telefonnummer |
| `email` | `TEXT` | ❌ | E-Mail-Adresse |
| `tax_number` | `TEXT` | ✅ | Steuernummer (Finanzamt) |
| `iban` | `TEXT` | ❌ | IBAN für Zahlungshinweis auf Rechnungen |
| `bank_name` | `TEXT` | ❌ | Bankname |
| `password_hash` | `TEXT` | ✅ | bcrypt-Hash des Anwendungspassworts |
| `logo_path` | `TEXT` | ❌ | Absoluter Pfad zur Logo-Datei (PNG/SVG) |
| `archive_path` | `TEXT` | ✅ | Basisordner für PDF-Archiv |
| `backup_path` | `TEXT` | ❌ | Zielordner für automatische DB-Backups |
| `receipts_path` | `TEXT` | ❌ | Basisordner für externe Belege (Jahresordner) |
| `reminder_template` | `TEXT` | ❌ | Mahnungsvorlage als Freitext |
| `created_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der Ersteinrichtung |
| `updated_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der letzten Änderung |

```sql
CREATE TABLE settings (
    id               INTEGER PRIMARY KEY CHECK (id = 1),
    company_name     TEXT    NOT NULL,
    owner_name       TEXT    NOT NULL,
    street           TEXT    NOT NULL,
    zip              TEXT    NOT NULL,
    city             TEXT    NOT NULL,
    phone            TEXT,
    email            TEXT,
    tax_number       TEXT    NOT NULL,
    iban             TEXT,
    bank_name        TEXT,
    password_hash    TEXT    NOT NULL,
    logo_path        TEXT,
    archive_path     TEXT    NOT NULL,
    backup_path      TEXT,
    receipts_path    TEXT,
    reminder_template TEXT,
    created_at       TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at       TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

---

### 2.2 `customers`

Speichert alle Kundenstammdaten.

| Spalte | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | `INTEGER` | ✅ | Primärschlüssel, auto-increment |
| `name` | `TEXT` | ✅ | Vor- und Nachname oder Firmenname |
| `street` | `TEXT` | ✅ | Straße und Hausnummer |
| `zip` | `TEXT` | ✅ | Postleitzahl |
| `city` | `TEXT` | ✅ | Ort |
| `email` | `TEXT` | ❌ | E-Mail-Adresse |
| `phone` | `TEXT` | ❌ | Telefonnummer |
| `notes` | `TEXT` | ❌ | Interne Notizen (Freitext) |
| `created_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der Anlage |
| `updated_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der letzten Änderung |

```sql
CREATE TABLE customers (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL,
    street     TEXT    NOT NULL,
    zip        TEXT    NOT NULL,
    city       TEXT    NOT NULL,
    email      TEXT,
    phone      TEXT,
    notes      TEXT,
    created_at TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

> **Löschschutz:** Ein Kunde darf nur gelöscht werden, wenn keine verknüpften Einträge in `documents` existieren. Diese Prüfung erfolgt auf Anwendungsebene vor dem DELETE.

---

### 2.3 `documents`

Speichert sowohl Angebote als auch Rechnungen. Der Typ wird über die Spalte `type` unterschieden.

| Spalte | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | `INTEGER` | ✅ | Primärschlüssel, auto-increment |
| `type` | `TEXT` | ✅ | `'offer'` = Angebot, `'invoice'` = Rechnung |
| `status` | `TEXT` | ✅ | `'draft'` / `'open'` / `'paid'` / `'cancelled'` |
| `is_historical` | `INTEGER` | ✅ | 0 = normales Dokument, 1 = historisch importiertes Dokument |
| `invoice_number` | `TEXT` | ❌ | Rechnungsnummer (nur bei Typ `invoice`, Format FNMMYY) |
| `customer_id` | `INTEGER` | ✅ | Fremdschlüssel → `customers.id` |
| `document_date` | `TEXT` | ✅ | Rechnungs-/Angebotsdatum (ISO-8601) |
| `service_date` | `TEXT` | ❌ | Leistungsdatum (ISO-8601) |
| `valid_until` | `TEXT` | ❌ | Gültig bis (nur bei Angeboten, ISO-8601) |
| `gross_total` | `REAL` | ✅ | Gesamtbruttobetrag (Summe aller Positionen) |
| `net_total` | `REAL` | ✅ | Gesamtnettobetrag (automatisch berechnet) |
| `tax_total` | `REAL` | ✅ | MwSt.-Gesamtbetrag (automatisch berechnet) |
| `pdf_path` | `TEXT` | ❌ | Absoluter Pfad zum gespeicherten PDF |
| `converted_from_id` | `INTEGER` | ❌ | Fremdschlüssel → `documents.id` (bei Konvertierung Angebot → Rechnung) |
| `notes` | `TEXT` | ❌ | Interne Notizen |
| `created_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der Anlage |
| `updated_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der letzten Änderung |

```sql
CREATE TABLE documents (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    type              TEXT    NOT NULL CHECK (type IN ('offer', 'invoice')),
    status            TEXT    NOT NULL CHECK (status IN ('draft', 'open', 'paid', 'cancelled'))
                              DEFAULT 'draft',
    invoice_number    TEXT    UNIQUE,
    customer_id       INTEGER NOT NULL REFERENCES customers (id),
    document_date     TEXT    NOT NULL,
    service_date      TEXT,
    valid_until       TEXT,
    gross_total       REAL    NOT NULL DEFAULT 0,
    net_total         REAL    NOT NULL DEFAULT 0,
    tax_total         REAL    NOT NULL DEFAULT 0,
    pdf_path          TEXT,
    converted_from_id INTEGER REFERENCES documents (id),
    notes             TEXT,
    created_at        TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at        TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

#### Status-Übergänge

```
draft ──→ open ──→ paid
  │                 │
  └──→ cancelled ←──┘
```

| Von | Nach | Auslöser |
|---|---|---|
| `draft` | `open` | PDF wurde gespeichert |
| `open` | `paid` | Benutzer markiert als bezahlt |
| `paid` | `open` | Benutzer setzt zurück auf offen |
| `open` / `paid` | `cancelled` | Stornierung (erzeugt Stornorechnung) |

> **GoBD-Hinweis:** Der Status `cancelled` ersetzt niemals die ursprüngliche Rechnung. Eine Stornierung erzeugt immer einen neuen Datensatz vom Typ `invoice` mit negativem Betrag.

---

### 2.4 `line_items`

Speichert die einzelnen Positionen eines Dokuments. Jede Position gehört zu genau einem Dokument.
Fachliche Eingabe: Der Benutzer gibt je Position einen Nettobetrag ein.  
`tax_amount` und `gross_amount` werden auf Anwendungsebene automatisch berechnet und gespeichert.

| Spalte | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | `INTEGER` | ✅ | Primärschlüssel, auto-increment |
| `document_id` | `INTEGER` | ✅ | Fremdschlüssel → `documents.id` |
| `position` | `INTEGER` | ✅ | Positionsnummer (Reihenfolge, beginnend bei 1) |
| `description` | `TEXT` | ✅ | Leistungsbeschreibung (Freitext, mehrzeilig) |
| `gross_amount` | `REAL` | ✅ | Bruttobetrag der Position (manuell eingegeben) |
| `net_amount` | `REAL` | ✅ | Nettobetrag (automatisch: Brutto ÷ 1,19) |
| `tax_amount` | `REAL` | ✅ | MwSt.-Betrag (automatisch: Brutto − Netto) |
| `tax_rate` | `REAL` | ✅ | Steuersatz in % (aktuell immer `19.0`) |
| `created_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der Anlage |

```sql
CREATE TABLE line_items (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    document_id INTEGER NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    position    INTEGER NOT NULL,
    description TEXT    NOT NULL,
    gross_amount REAL   NOT NULL,
    net_amount  REAL    NOT NULL,
    tax_amount  REAL    NOT NULL,
    tax_rate    REAL    NOT NULL DEFAULT 19.0,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    UNIQUE (document_id, position)
);
```

> `ON DELETE CASCADE`: Wird ein Dokument gelöscht, werden alle zugehörigen Positionen automatisch mitgelöscht.

---

### 2.5 `external_invoices`

Speichert externe Belege (Betriebsausgaben) des Auftraggebers. Diese Tabelle ist **unabhängig** von `customers` und `documents` – sie dient ausschließlich der Erfassung von Ausgaben für steuerliche Zwecke.

| Spalte | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `id` | `INTEGER` | ✅ | Primärschlüssel, auto-increment |
| `file_path` | `TEXT` | ✅ | Absoluter Pfad zur PDF-Datei auf dem Rechner |
| `year` | `INTEGER` | ✅ | Steuerjahr (z. B. `2025`) |
| `date` | `TEXT` | ✅ | Belegdatum (ISO-8601, manuell eingegeben) |
| `description` | `TEXT` | ✅ | Wofür (z. B. „Tanken", „Werkzeug kaufen") |
| `category` | `TEXT` | ❌ | Kostenkategorie (z. B. „Fahrtkosten", „Material") |
| `gross_amount` | `REAL` | ✅ | Bruttobetrag (manuell eingegeben) |
| `net_amount` | `REAL` | ✅ | Nettobetrag (automatisch: Brutto ÷ 1,19) |
| `tax_amount` | `REAL` | ✅ | Vorsteuer (automatisch: Brutto − Netto) |
| `tax_rate` | `REAL` | ✅ | Steuersatz in % (aktuell immer `19.0`) |
| `created_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der Erfassung |
| `updated_at` | `TEXT` | ✅ | ISO-8601-Zeitstempel der letzten Änderung |

```sql
CREATE TABLE external_invoices (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path    TEXT    NOT NULL,
    year         INTEGER NOT NULL,
    date         TEXT    NOT NULL,
    description  TEXT    NOT NULL,
    category     TEXT,
    gross_amount REAL    NOT NULL,
    net_amount   REAL    NOT NULL,
    tax_amount   REAL    NOT NULL,
    tax_rate     REAL    NOT NULL DEFAULT 19.0,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

---

## 3 Beziehungen

| Beziehung | Typ | Beschreibung |
|---|---|---|
| `customers` → `documents` | 1 : n | Ein Kunde kann viele Dokumente haben |
| `documents` → `line_items` | 1 : n | Ein Dokument hat eine oder mehrere Positionen |
| `documents` → `documents` | 1 : 0..1 | Ein Angebot kann in eine Rechnung umgewandelt werden (`converted_from_id`) |
| `external_invoices` | — | Steht für sich allein, keine Fremdschlüssel |

### Fremdschlüssel aktivieren

SQLite deaktiviert Fremdschlüssel standardmäßig. Die Anwendung muss bei jedem Datenbankverbindungsaufbau folgendes ausführen:

```sql
PRAGMA foreign_keys = ON;
```

---

## 4 Berechnungsregeln

Alle Betragsberechnungen erfolgen auf **Anwendungsebene** vor dem Speichern. In der Datenbank werden ausschließlich bereits berechnete Werte gespeichert.

### Berechnung aus Nettobetrag (Eingabe)

```
tax_amount   = ROUND(net_amount * 0.19, 2)
gross_amount = ROUND(net_amount + tax_amount, 2)
```

### Dokumentsummen (aus Positionen)

```
net_total   = SUM(line_items.net_amount)
tax_total   = ROUND(net_total * 0.19, 2)
gross_total = ROUND(net_total + tax_total, 2)
```

### Rechnungsnummer (Format FNMMYY)

```
invoice_number = 'F'
               + LPAD(count_this_month + 1, 2, '0')   -- laufende Nr. im Monat
               + LPAD(month, 2, '0')                   -- Monat
               + RIGHT(year, 2)                        -- Jahr zweistellig
```

Beispiel: Die zweite Rechnung im März 2025 → `F020325`

Die laufende Nummer wird durch folgende Abfrage ermittelt:

```sql
SELECT COUNT(*) + 1
FROM documents
WHERE type = 'invoice'
  AND strftime('%m', document_date) = strftime('%m', 'now')
  AND strftime('%Y', document_date) = strftime('%Y', 'now');
```

---

## 5 Indexe

Indexe werden angelegt, um häufige Abfragen zu beschleunigen.

```sql
-- Kundensuche (Live-Suche nach Name)
CREATE INDEX idx_customers_name
    ON customers (name);

-- Rechnungsübersicht (Sortierung nach Datum, Filter nach Status)
CREATE INDEX idx_documents_date_status
    ON documents (document_date DESC, status);

-- Offene Rechnungen (häufige Filterabfrage)
CREATE INDEX idx_documents_type_status
    ON documents (type, status);

-- Positionen je Dokument (beim Laden eines Dokuments)
CREATE INDEX idx_line_items_document
    ON line_items (document_id, position);

-- Externe Belege nach Jahr (Jahresübersicht)
CREATE INDEX idx_external_invoices_year
    ON external_invoices (year, date);
```

---

## 6 Migrationen

Die Datenbankversion wird in einer separaten Tabelle verwaltet. Beim Anwendungsstart prüft die Anwendung die aktuelle Version und führt ggf. ausstehende Migrationen aus.

```sql
CREATE TABLE schema_migrations (
    version    INTEGER PRIMARY KEY,
    applied_at TEXT NOT NULL DEFAULT (datetime('now')),
    description TEXT
);
```

### Migrationsstrategie

1. Jede Schemaänderung erhält eine eindeutige Versionsnummer (z. B. `1`, `2`, `3`, …)
2. Migrationen werden als SQL-Dateien versioniert (z. B. `migrations/001_initial.sql`)
3. Beim Start werden alle noch nicht angewandten Migrationen in aufsteigender Reihenfolge ausgeführt
4. Ein Rollback ist nicht vorgesehen – Datensicherung vor Migrationen empfohlen

### Migration 001 – Initiales Schema

Enthält die vollständige Erstanlage aller Tabellen und Indexe (siehe Kap. 2 und 5).

---

## 7 Designentscheidungen

### Warum SQLite?

- Keine separate Datenbankinstallation notwendig
- Die gesamte Datenbank ist eine einzelne Datei → einfaches Backup (Datei kopieren)
- Für Einzelnutzer-Desktopanwendungen mit < 10.000 Datensätzen mehr als ausreichend
- Bewährt in vergleichbaren Produkten (z. B. wird SQLite von Firefox, WhatsApp und vielen weiteren Produkten eingesetzt)

### Warum Beträge als REAL und nicht als INTEGER (Cent)?

Die Eingabe erfolgt als Euro-Betrag mit zwei Dezimalstellen. Alle gespeicherten Werte werden auf **2 Dezimalstellen gerundet** (`ROUND(..., 2)`), bevor sie in die Datenbank geschrieben werden. Rundungsdifferenzen werden auf Anwendungsebene behandelt.

### Warum Zeitstempel als TEXT?

SQLite hat keinen nativen Datums-/Zeittyp. Die Speicherung als ISO-8601-String (`YYYY-MM-DD HH:MM:SS`) ist die offizielle SQLite-Empfehlung und erlaubt direkte Vergleiche und Sortierungen als Text.

### Warum keine separaten Angebots- und Rechnungstabellen?

Angebote und Rechnungen teilen dieselbe Struktur (Kunde, Positionen, Beträge, PDF-Pfad). Eine gemeinsame Tabelle mit `type`-Spalte vermeidet Redundanz und vereinfacht die Konvertierung Angebot → Rechnung (kein Datentransfer zwischen Tabellen nötig).

---

*Version 1.0 · Stand: März 2025 · Baum Performance Stahl*


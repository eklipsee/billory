# Pflichtenheft
## Rechnungs- und Angebotssoftware

**Auftraggeber:** Baum Performance Stahl  
**Projektart:** Individualsoftware (Einzelunternehmer)  
**Dokument:** Pflichtenheft  
**Version:** 1.0  
**Stand:** März 2025  
**Status:** Entwurf  

---

## Inhaltsverzeichnis

1. [Einleitung](#1-einleitung)
2. [Systemarchitektur und Technologie](#2-systemarchitektur-und-technologie)
3. [Funktionale Anforderungen](#3-funktionale-anforderungen)
4. [PDF-Dokumentenlayout](#4-pdf-dokumentenlayout)
5. [Anforderungstabelle](#5-anforderungstabelle)
6. [Rechtliche und steuerliche Anforderungen](#6-rechtliche-und-steuerliche-anforderungen)
7. [Qualitäts- und Nichtfunktionale Anforderungen](#7-qualitäts--und-nichtfunktionale-anforderungen)
8. [Installation und Betrieb](#8-installation-und-betrieb)
9. [Abnahmekriterien](#9-abnahmekriterien)

---

## 1 Einleitung

### 1.1 Zweck des Dokuments

Dieses Pflichtenheft beschreibt die technische und fachliche Umsetzung der Rechnungs- und Angebotssoftware für Baum Performance Stahl. Es konkretisiert die im Lastenheft formulierten Anforderungen und legt fest, **wie** das System die definierten Ziele erfüllt.

Das Dokument dient als verbindliche Grundlage für Entwicklung, Test und Abnahme.

### 1.2 Projektbeschreibung

Es wird eine lokal installierte Windows-Desktop-Anwendung entwickelt, die folgende Kernfunktionen bereitstellt:

- Erstellung und Verwaltung von Angeboten und Rechnungen als PDF
- Kundenverwaltung mit Stammdaten und Suchfunktion
- Automatische, fortlaufende Rechnungsnummerierung (Format FNMMYY)
- Zahlungsstatus-Verwaltung und Offene-Posten-Übersicht
- Mahnungserstellung aus Vorlage
- Verwaltung externer Rechnungs-PDFs mit Steuerberechnung
- DATEV-kompatibler Datenexport

### 1.3 Abgrenzung

Folgende Funktionen sind ausdrücklich **nicht** Bestandteil des Projekts:

- Cloud-Speicherung oder Netzwerkbetrieb
- Mehrbenutzerbetrieb
- Vollständige Buchhaltungssoftware
- Online-Zahlungsabwicklung
- Automatische Verarbeitung eingescannter Dokumente
- Digitale Signatur von Dokumenten

---

## 2 Systemarchitektur und Technologie

### 2.1 Technologie-Stack

| Bereich | Technologie | Begründung |
|---|---|---|
| Plattform | Windows 10 / 11 (64-Bit) | Vorgabe Auftraggeber |
| Desktop-Framework | Electron.js | Stellt die Desktop-Hülle bereit |
| Frontend | React.js + TypeScript | Komponentenbasierte UI |
| Backend | Spring Boot (Java 21) | REST-API, Geschäftslogik, Validierung |
| Build-Tool Backend | Maven | Standard-Build für Spring Boot |
| Datenbank | SQLite | Lokale dateibasierte Speicherung |
| ORM / Datenzugriff | Spring Data JPA + Hibernate | Entity-Mapping und Repository-Zugriff |
| Migrationen | Flyway | Versionssichere Datenbankmigrationen |
| PDF-Erzeugung | OpenPDF | PDF-Erzeugung im Java-Backend |
| Packaging | Electron Builder + NSIS | Windows-Installer |

### 2.2 Systemkomponenten

Die Anwendung ist in folgende Schichten aufgeteilt:

- **UI-Layer** – React-Komponenten für Ansichten, Formulare und Tabellen
- **Desktop-Layer** – Electron als lokale Desktop-Hülle
- **Backend-Layer** – Spring Boot REST-API für Geschäftslogik und Validierung
- **Service-Layer** – Java-Services für Nummerierung, Berechnungen, PDF-Generierung und Statuslogik
- **Repository-Layer** – Spring Data JPA Repositories für SQLite-Zugriffe
- **PDF-Service** – Erzeugung von Angebots- und Rechnungs-PDFs mit OpenPDF
- **Export-Service** – DATEV-/CSV-Export
- **Auth-Modul** – Passwortschutz beim Start der Anwendung

### 2.3 Datenhaltung

Alle Anwendungsdaten werden lokal in einer SQLite-Datenbank gespeichert.

**Speicherort:** `%APPDATA%\BaumPerformanceStahl\` (konfigurierbar)

| Tabelle | Inhalt |
|---|---|
| `customers` | Kundenstammdaten (Name, Adresse, E-Mail, Telefon, Notizen) |
| `documents` | Angebote und Rechnungen (Typ, Status, Nummer, Datum, Gesamtbetrag) |
| `line_items` | Positionen je Dokument (Beschreibung, Netto, MwSt., Brutto) |
| `external_invoices` | Externe Belege (Pfad, Jahr, Datum, Beschreibung, Kategorie, Brutto, Netto, MwSt.) |
| `settings` | Konfiguration (Passwort-Hash, Firmendaten, Archivpfad) |

### 2.4 Risiken und offene Punkte

| Risiko | Beschreibung | Maßnahme |
|---|---|---|
| GoBD-Konformität | Die Anforderungen sind technisch umsetzbar, eine rechtliche Prüfung ist jedoch nicht Bestandteil dieses Projekts | Auftraggeber prüft ggf. mit Steuerberater |
| DATEV-Format | DATEV pflegt mehrere Exportformate; das exakte Zielformat muss vor Implementierung verifiziert werden | Abstimmung mit Auftraggeber / DATEV-Dokumentation |
| PDF-Layout | Musterrechnung liegt vor (siehe Anhang) | Wird als Vorlage für den PDF-Service verwendet |
| Firmenlogo | Logo liegt vor (siehe Anhang) | Wird in den PDF-Service integriert |

---

## 3 Funktionale Anforderungen

### 3.1 Passwortschutz

Beim Start der Anwendung wird ein Passwort abgefragt. Ohne korrektes Passwort ist kein Zugriff möglich.

- Passwort wird als **bcrypt-Hash** gespeichert (kein Klartext)
- Einmalige Einrichtung beim ersten Start
- Änderung des Passworts über die Einstellungen möglich
- Mindestlänge: 8 Zeichen

### 3.2 Kundenverwaltung

#### Datenfelder je Kunde

| Feld | Typ | Pflicht |
|---|---|---|
| Name | Text | ✅ |
| Straße | Text | ✅ |
| PLZ / Ort | Text | ✅ |
| E-Mail | Text | ❌ |
| Telefon | Text | ❌ |
| Notizen | Freitext | ❌ |

#### Funktionen

- Neuanlage eines Kunden über ein Formular
- Bearbeitung aller Kundendaten
- Löschen eines Kunden (nur wenn keine Dokumente verknüpft sind)
- Live-Volltextsuche über den Kundennamen
- Automatische Übernahme der Kundendaten bei Dokumenterstellung

### 3.3 Angebotserstellung

- Anlage eines Angebots mit Zuweisung zu einem Kunden
- Beliebig viele Positionen (Freitext + Nettobetrag)
- Automatische Berechnung: 19 % MwSt. und Gesamtbrutto
- Angebotsdatum manuell eingebbar (Standard: heute)
- Keine eigene Nummerierung für Angebote
- Speicherung als PDF im Archivordner
- Hinweis auf Widerrufsrecht wird automatisch ins PDF eingefügt
- Umwandlung eines Angebots in eine Rechnung per Klick

### 3.4 Rechnungserstellung

- Anlage einer Rechnung (neu oder aus Angebot konvertiert)
- Automatische Vergabe der Rechnungsnummer gemäß Format FNMMYY
- Beliebig viele Positionen (Freitext + Nettobetrag)
- Automatische Berechnung: 19 % MwSt. und Gesamtbrutto
- Rechnungsdatum manuell eingebbar (Standard: heute)
- Speicherung als PDF im Archivordner
- Datenschutzhinweis wird automatisch ins PDF eingefügt

### Historische Rechnungen

Zusätzlich zur normalen Rechnungserstellung können historische Rechnungen manuell in das System übernommen werden.

Eigenschaften historischer Rechnungen:

- vorhandene Rechnungsnummer wird manuell übernommen
- Rechnungsdatum und Leistungsdatum werden manuell gesetzt
- Status kann direkt als `OPEN` oder `PAID` gesetzt werden
- automatische Rechnungsnummernvergabe erfolgt nicht
- optional kann ein vorhandenes Original-PDF mit dem Dokument verknüpft werden

Historische Rechnungen dienen der nachträglichen Übernahme bereits existierender Ausgangsrechnungen in Papierform oder als PDF.

### Verknüpfung vorhandener PDFs

Für historische Rechnungen kann ein bereits vorhandenes Original-PDF mit dem Dokument verknüpft werden.

Dabei gilt:

- die Quelldatei muss eine PDF-Datei sein
- die Datei wird in das definierte Archiv kopiert
- der neue Archivpfad wird im Dokument gespeichert
- die Verknüpfung ist nur für historische Dokumente zulässig

#### Rechnungsnummerierung

Format: `F[lfd.Nr.][MM][JJ]` – Beispiel: `F020325`

| Teil | Bedeutung | Beispiel |
|---|---|---|
| `F` | Präfix = Rechnung | F |
| `02` | Laufende Nummer (pro Monat, zweistellig) | 02 |
| `03` | Monat (zweistellig) | 03 = März |
| `25` | Jahr (zweistellig) | 25 = 2025 |

Die Nummer wird beim Speichern automatisch vergeben und ist danach nicht mehr änderbar.

### 3.5 Rechnungsübersicht und Zahlungsstatus

- Tabellarische Übersicht aller Rechnungen (Nummer, Kunde, Betrag, Datum, Status)
- Filter: alle Rechnungen / nur offene Rechnungen
- Statuswechsel „offen" ↔ „bezahlt" per Klick
- Schnellzugriff auf das PDF-Dokument je Rechnung
- Gesamtsumme offener Rechnungen wird angezeigt

### 3.6 Mahnungserstellung

- Auswahl einer offenen Rechnung als Grundlage
- Automatische Übernahme von Kundendaten und Rechnungsnummer
- Mahnung basiert auf einem vordefinierten, konfigurierbaren Mustertext
- Speicherung der Mahnung als PDF

### 3.7 Verwaltung externer Belege (Betriebsausgaben)

Der Auftraggeber fotografiert Belege (z. B. Tankquittungen, Materialrechnungen) und speichert diese als PDFs in jahresbezogenen Ordnern (z. B. `Belege/2025/`). Die Anwendung ermöglicht es, diese Belege gesammelt zu öffnen, Beträge manuell einzugeben und die enthaltene Vorsteuer automatisch zu berechnen. Ziel ist eine Übersicht der jährlichen Betriebsausgaben als Grundlage für die Steuererklärung (EÜR).

#### Datenfelder je externem Beleg

| Feld | Beschreibung | Pflicht |
|---|---|---|
| `file_path` | Pfad zur PDF-Datei auf dem Rechner | ✅ |
| `year` | Jahresordner / Steuerjahr | ✅ |
| `date` | Belegdatum (manuell eingebbar) | ✅ |
| `description` | Wofür (z. B. „Tanken", „Werkzeug") | ✅ |
| `category` | Kostenkategorie (z. B. „Fahrtkosten", „Material") | ❌ |
| `gross_amount` | Bruttobetrag (manuell eingegeben) | ✅ |
| `net_amount` | Nettobetrag (automatisch: Brutto ÷ 1,19) | — |
| `tax_amount` | MwSt.-Betrag (automatisch: Brutto − Netto) | — |

#### Funktionen

- Konfiguration eines Basisordners in den Einstellungen; Unterordner je Jahr werden automatisch erkannt
- Anzeige aller PDFs des gewählten Jahres als Liste (Dateiname, Datum, Betrag sofern bereits erfasst)
- Vorschau des gewählten PDFs innerhalb der Anwendung (eingebetteter Viewer)
- Manuelle Eingabe von Datum, Beschreibung, Kategorie und Bruttobetrag je Beleg
- Automatische Berechnung von Netto und MwSt. aus dem eingegebenen Bruttobetrag
- Jahresübersicht: Summe aller Bruttoausgaben, Summe Netto, Summe abziehbare Vorsteuer
- Export der Jahresübersicht als CSV für den Steuerberater

### 3.8 DATEV-Export

- Export aller Rechnungen (oder eines gewählten Zeitraums) als CSV-Datei
- Format entspricht DATEV-Buchungsstapel-Standard
- Felder: Rechnungsnummer, Datum, Kundennummer, Netto, MwSt., Brutto, Belegtext
- Dateiname: `DATEV_Export_MMJJ.csv`

---

## 4 PDF-Dokumentenlayout

### 4.1 Allgemeine Layoutvorgaben

- DIN A4, Hochformat
- Firmenlogo oben links (wird vom Auftraggeber als PNG/SVG bereitgestellt)
- Positionspreise werden im Rechnungslayout als Nettobeträge ausgewiesen
- Summenblock enthält Nettobetrag, Umsatzsteuer und Gesamtbetrag
- Absenderblock und Empfängeranschrift gemäß DIN 5008
- Beträge rechtsbündig, Dezimalformat: `1.234,56 €`
- Schriftart: Arial, 11 pt Fließtext

### 4.2 Pflichtbestandteile einer Rechnung (gem. § 14 UStG)

- Vollständiger Name und Anschrift des Ausstellers
- Vollständiger Name und Anschrift des Leistungsempfängers
- Steuernummer des Ausstellers
- Rechnungsdatum und Leistungsdatum
- Fortlaufende Rechnungsnummer
- Beschreibung der Leistungen
- Nettobetrag je Position, Steuersatz (19 %), MwSt.-Betrag, Gesamtbrutto
- Datenschutzhinweis (gem. DSGVO / Art. 13)

### 4.3 Pflichtbestandteile eines Angebots

- Alle Felder wie Rechnung (ohne Rechnungsnummer und Steuerangaben)
- Gültigkeitsdatum des Angebots
- Hinweis auf Widerrufsrecht (gem. § 355 BGB)


### Archivstruktur

Erzeugte und verknüpfte PDF-Dokumente werden strukturiert im Archivordner gespeichert.

Ablagestruktur:

- `<archivePath>/Rechnungen/<Jahr>/`
- `<archivePath>/Angebote/<Jahr>/`

Externe Belege werden separat gespeichert unter:

- `<receiptsPath>/<Jahr>/`

---

## 5 Anforderungstabelle

> **Legende:** `Muss` = Abnahmekriterium · `Soll` = hohe Priorität · `Kann` = nice-to-have

| ID | Anforderung | Prio | Quelle |
|---|---|---|---|
| FA-01 | Angebote können erstellt und als PDF gespeichert werden | Muss | Kap. 3.3 |
| FA-02 | Rechnungen können erstellt und als PDF gespeichert werden | Muss | Kap. 3.4 |
| FA-03 | Kundendaten können gespeichert und bearbeitet werden | Muss | Kap. 3.2 |
| FA-04 | Rechnungen werden automatisch nummeriert (Format FNMMYY) | Muss | Kap. 3.4 |
| FA-05 | Angebote können per Klick in Rechnungen umgewandelt werden | Muss | Kap. 3.3 |
| FA-06 | Rechnungen können archiviert und verwaltet werden | Muss | Kap. 3.5 |
| FA-07 | Externe Belege können jahresweise geöffnet, erfasst und ausgewertet werden | Muss | Kap. 3.7 |
| FA-08 | DATEV-kompatibler CSV-Export ist möglich | Muss | Kap. 3.8 |
| FA-09 | Zahlungsstatus (offen / bezahlt) je Rechnung verwaltbar | Muss | Kap. 3.5 |
| FA-10 | Muster-Mahnung mit automatischer Kundendatenübernahme | Soll | Kap. 3.6 |
| FA-11 | Anwendung ist durch Passwort geschützt | Soll | Kap. 3.1 |
| FA-12 | Live-Suche nach Kunden über den Namen | Soll | Kap. 3.2 |
| FA-13 | Übersicht über offene Rechnungen mit Gesamtsumme | Soll | Kap. 3.5 |
| FA-14 | Jahresübersicht der Betriebsausgaben mit Vorsteuerberechnung und CSV-Export | Soll | Kap. 3.7 |
| FA-15 | Konfigurierbarer Mahnungsvorlagentext | Kann | Kap. 3.6 |
| NFA-01 | Normale Aktionen reagieren in max. 2 Sekunden | Muss | Kap. 7 |
| NFA-02 | Komplexe Abfragen reagieren in max. 5 Sekunden | Soll | Kap. 7 |
| NFA-03 | Keine Datenverluste bei Fehler (Transaktions-Rollback) | Muss | Kap. 7 |
| NFA-04 | Verständliche Fehlermeldungen bei Fehlerfall | Muss | Kap. 7 |
| NFA-05 | Installation per Windows Setup (.exe) | Muss | Kap. 2 |

---

## 6 Rechtliche und steuerliche Anforderungen

### 6.1 Steuerliche Vorgaben

| Aspekt | Vorgabe |
|---|---|
| Umsatzsteuersatz | 19 % (fest eingestellt) |
| Kleinunternehmerregelung | nicht anwendbar |
| Aufbewahrungsfrist | 10 Jahre |
| GoBD-Konformität | Archivierung unveränderlicher PDFs, keine nachträgliche Änderung |

### 6.2 GoBD-konforme Archivierung

- Gespeicherte Rechnungs-PDFs sind nach dem Speichern nicht mehr änderbar
- Rechnungsnummern sind eindeutig und fortlaufend; Rücknummerierung ist nicht möglich
- Stornierung erfolgt über eine Stornorechnung, nicht durch Überschreiben
- Alle Dokumente werden mit Zeitstempel archiviert

### 6.3 Pflichtangaben auf Dokumenten

**Angebote:** Hinweis auf das Widerrufsrecht gem. § 355 BGB wird als Standardtext eingefügt.

**Rechnungen:** Datenschutzhinweis gem. Art. 13 DSGVO wird als Standardtext eingefügt.

---

## 7 Qualitäts- und Nichtfunktionale Anforderungen

### 7.1 Performance

- Normaler Seitenaufbau, Suche, Formularoperationen: max. **2 Sekunden**
- PDF-Generierung, komplexe Datenbankabfragen: max. **5 Sekunden**
- Anwendungsstart (inkl. Passwort-Dialog): max. **5 Sekunden**
- Ziel: Rechnung erstellen und als PDF speichern innerhalb von **2 Minuten**

### 7.2 Stabilität und Fehlerbehandlung

- Datenbank-Transaktionen sichern Konsistenz bei Fehlern (Rollback)
- Fehler werden dem Benutzer in verständlicher Sprache angezeigt (kein Stacktrace)
- Bereits gespeicherte Daten bleiben bei Anwendungsfehlern erhalten
- Automatische Sicherungskopie der Datenbank beim Anwendungsstart

### 7.3 Sicherheit

- Passwort wird als bcrypt-Hash gespeichert (kein Klartext)
- Keine Netzwerkkommunikation – vollständig offline
- Schreibzugriff nur auf definierten Datenordner

### 7.4 Usability

- Deutsche Benutzeroberfläche
- Konsistentes Design, angelehnt an moderne Windows-Anwendungen
- Wichtige Aktionen (neu, speichern, PDF) als Buttons gut sichtbar
- Pflichtfelder werden visuell hervorgehoben

---

## 8 Installation und Betrieb

### 8.1 Installation

- Bereitstellung als Windows-Installer (`.exe`), erstellt mit Electron Builder / NSIS
- Installation ohne Admin-Rechte möglich (User-Level-Installer)
- Eintrag im Windows-Startmenü, optional Desktop-Verknüpfung
- Erststart: Passwort einrichten und Firmenstammdaten eingeben

### 8.2 Datensicherung

Die Datensicherung liegt in der Verantwortung des Auftraggebers und erfolgt manuell (z. B. auf externer Festplatte). Der Backup-Pfad ist in der Anwendung konfigurierbar.

Zusätzlich erzeugt die Anwendung beim Start automatisch eine Sicherungskopie der Datenbank im konfigurierten Backup-Ordner.

### 8.3 Updates

Updates werden als neue Installer-Version bereitgestellt. Beim Update bleiben vorhandene Daten erhalten. Eine automatische Update-Funktion ist nicht vorgesehen.

---

## 9 Abnahmekriterien

Das Projekt gilt als erfolgreich abgeschlossen, wenn alle **Muss-Anforderungen** erfüllt und folgende Abnahmetests bestanden sind:

| ID | Abnahmetest | Erwartetes Ergebnis |
|---|---|---|
| AT-01 | Angebot mit 3 Positionen erstellen und als PDF speichern | PDF wird erzeugt, Beträge korrekt berechnet |
| AT-02 | Angebot in Rechnung umwandeln | Rechnungsnummer wird automatisch vergeben |
| AT-03 | Rechnung in unter 2 Minuten erstellen | PDF gespeichert, Zeitlimit eingehalten |
| AT-04 | Zahlungsstatus auf „bezahlt" ändern | Status in Übersicht aktualisiert |
| AT-05 | Kundendaten anlegen und bearbeiten | Daten korrekt gespeichert |
| AT-06 | Externes Rechnungs-PDF öffnen und Betrag eingeben | Netto und MwSt. korrekt berechnet |
| AT-07 | DATEV-Export durchführen | CSV-Datei im korrekten Format erstellt |
| AT-08 | Anwendungsstart mit falschem Passwort | Zugriff verweigert |

---

*Version 1.0 · Stand: März 2025 · Baum Performance Stahl*

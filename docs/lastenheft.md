# Lastenheft

## Projekt
**Rechnungs- und Angebotssoftware (Desktop-Anwendung)**

**Auftraggeber:** Baum Performance Stahl  
**Projektart:** Individualsoftware für einen Einzelunternehmer  

---

# 1 Zielbestimmung

## 1.1 Ausgangssituation

Der Auftraggeber erstellt Angebote und Rechnungen derzeit mit  
Microsoft Excel und archiviert diese teilweise zusätzlich in Papierform.

Eine strukturierte digitale Verwaltung der Kundendaten und Dokumente existiert nicht. Dadurch entstehen mehrere Probleme:

- erhöhter manueller Aufwand bei der Erstellung von Dokumenten  
- fehlende Übersicht über bereits erstellte Rechnungen  
- erschwerte Nachverfolgung von Zahlungsständen  
- erhöhtes Risiko von Fehlern bei der Rechnungsnummerierung  
- unstrukturierte Archivierung von Dokumenten  

---

## 1.2 Projektziel

Ziel des Projekts ist die Entwicklung einer **Desktop-Anwendung zur Erstellung und Verwaltung von Angeboten und Rechnungen**.

Die Software soll die tägliche Verwaltungsarbeit des Auftraggebers vereinfachen und eine strukturierte digitale Dokumentenverwaltung ermöglichen.

### Konkrete Zieldefinition

- Reduzierung des manuellen Arbeitsaufwands bei der Dokumentenerstellung
- Vermeidung von Fehlern bei der Rechnungsnummerierung
- Zentrale Verwaltung von Kunden- und Dokumentendaten
- Schnelle Erstellung von Angeboten und Rechnungen
- Verbesserte Übersicht über bestehende Dokumente
- Erstellung einer Rechnung innerhalb von maximal **2 Minuten**
- Automatische Generierung fortlaufender Rechnungsnummern
- Strukturierte lokale Archivierung aller Dokumente
- Möglichkeit zur Umwandlung eines Angebots in eine Rechnung
- Integration rechtlich notwendiger Hinweise in Dokumenten
- Exportmöglichkeit für Steuerzwecke (DATEV-kompatibler Export)

---

## 1.3 Nicht-Ziele (Abgrenzung)

Folgende Funktionen sind **nicht Bestandteil** des Projekts:

- Cloud-Speicherung
- Mehrbenutzerbetrieb
- vollständige Buchhaltungssoftware
- Online-Zahlungsabwicklung
- automatische Verarbeitung eingescannter Rechnungen

---

# 2 Produktübersicht

Bei dem Produkt handelt es sich um eine **lokal installierte Windows-Desktop-Anwendung**.

Die Anwendung wird ausschließlich auf einem einzelnen Computer betrieben und benötigt keine Netzwerkverbindung.

### Zielplattform

- Windows 10 (64-Bit)
- Windows 11 (64-Bit)

### Installation

- klassische Windows-Installation (Setup)
- Eintrag im Windows-Startmenü

### Mehrbenutzerbetrieb

Nicht vorgesehen.

---

# 3 Rahmenbedingungen

## 3.1 Technische Rahmenbedingungen

- Nutzung auf einem einzelnen Computer
- Daten werden lokal gespeichert
- Wechsel des Geräts ist durch Datensicherung möglich
- keine Internetverbindung erforderlich

### Datensicherung

- Backup erfolgt manuell durch den Auftraggeber  
- Speicherung auf externer Festplatte vorgesehen

### Zugriffsschutz

Die Anwendung soll durch ein Passwort geschützt werden.

---

## 3.2 Rechtliche Rahmenbedingungen

Die Software muss grundlegende rechtliche Anforderungen für die Erstellung von Rechnungen erfüllen.

### Steuerliche Rahmenbedingungen

- Umsatzsteuersatz: **19 %**
- Kleinunternehmerregelung: **nicht relevant**
- Aufbewahrungsfrist für Rechnungen: **10 Jahre**
- GoBD-konforme Archivierung erforderlich

### Rechnungsnummer

Die Rechnungsnummern müssen **fortlaufend und eindeutig** sein.

Format laut Vorgabe des Auftraggebers: FNMMYY

Beispiel: F020325

Bedeutung:

- F = Rechnung
- 02 = zweite Rechnung
- 03 = Monat (März)
- 25 = Jahr (2025)

### Rechtliche Hinweise in Dokumenten

Angebote müssen einen **Hinweis auf das Widerrufsrecht** enthalten.

Rechnungen müssen einen **Hinweis zum Datenschutz** enthalten.

---

# 4 Funktionale Anforderungen

## 4.1 Kundenverwaltung

Die Anwendung soll eine Verwaltung von Kundendaten ermöglichen.

### Zu speichernde Daten

- Name
- Adresse
- E-Mail-Adresse
- Telefonnummer
- Notizen

### Funktionen

- Anlegen neuer Kunden
- Bearbeiten bestehender Kundendaten
- Suche nach Kunden über den Namen

---

## 4.2 Angebotsverwaltung

Die Anwendung soll die Erstellung und Verwaltung von Angeboten ermöglichen.

### Funktionen

- Erstellung von Angeboten
- Speicherung von Angeboten als PDF
- Umwandlung eines Angebots in eine Rechnung

Ein eigenes Nummernsystem für Angebote ist nicht erforderlich.

---

## 4.3 Rechnungsverwaltung

Die Anwendung soll die Erstellung und Verwaltung von Rechnungen ermöglichen.

### Funktionen

- Erstellung von Rechnungen
- automatische Generierung der Rechnungsnummer
- Speicherung der Rechnung als PDF
- Übersicht über alle Rechnungen

### Zahlungsstatus

Rechnungen sollen einen Status besitzen:

- offen
- bezahlt

Zusätzlich soll eine Übersicht über **offene Rechnungen** verfügbar sein.

### Mahnungen

Die Anwendung soll die Erstellung einer **Muster-Mahnung** ermöglichen, bei der Kundendaten automatisch übernommen werden.

---

## 4.4 Positions- / Leistungsverwaltung

Rechnungen und Angebote bestehen aus einzelnen Positionen.

### Anforderungen

- Freitextbeschreibung der Leistung
- mehrere Positionen pro Dokument möglich
- ausschließlich Festpreispositionen

### Preisangaben

Preisangaben erfolgen je Position als **Nettobetrag**.

Umsatzsteuer und Gesamtbruttobetrag werden automatisch berechnet.

---

## 4.5 Verwaltung externer Rechnungsdokumente

Der Auftraggeber verwaltet zusätzlich externe Rechnungsdokumente (PDF).

### Anforderungen

- Möglichkeit, einen Ordner mit Rechnungs-PDFs innerhalb der Software zu öffnen
- Anzeige dieser PDFs innerhalb der Anwendung
- manuelle Eingabe von Rechnungsbeträgen durch den Benutzer
- automatische Berechnung der enthaltenen Steuer aus den eingegebenen Beträgen

---

# 5 Dokumentanforderungen

### PDF-Dokumente

Die erzeugten Angebote und Rechnungen sollen als **PDF-Dateien** gespeichert werden.

### Layout

- Firmenlogo wird vom Auftraggeber bereitgestellt
- Layout orientiert sich an einer vorhandenen Musterrechnung

Eine digitale Signatur der Dokumente ist **nicht erforderlich**.

---

# 6 Datenhaltung

### Speicherung

Alle Daten werden **lokal auf dem Rechner** gespeichert.

### Backup

Datensicherung erfolgt manuell durch den Auftraggeber  
(z. B. auf externer Festplatte).

### Export

Für steuerliche Zwecke soll ein **DATEV-kompatibler Datenexport** möglich sein.

---

# 7 Qualitätsanforderungen

### Performance

- normale Aktionen: maximal **2 Sekunden**
- komplexere Abfragen: maximal **5 Sekunden**

### Stabilität

Die Anwendung soll stabil laufen und während normaler Nutzung nicht abstürzen.

Bei Fehlern müssen verständliche Fehlermeldungen angezeigt werden, ohne dass bereits gespeicherte Daten verloren gehen.

---

# 8 Abnahmekriterien

Das Projekt gilt als erfolgreich abgeschlossen, wenn folgende Funktionen vorhanden sind:

- Angebote können erstellt und als PDF gespeichert werden
- Rechnungen können erstellt und als PDF gespeichert werden
- Kundendaten können gespeichert und bearbeitet werden
- Rechnungen werden automatisch nummeriert
- Angebote können in Rechnungen umgewandelt werden
- Rechnungen können archiviert und verwaltet werden
- externe Rechnungs-PDFs können geöffnet und ausgewertet werden
- DATEV-Export ist möglich

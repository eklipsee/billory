# Billory – Rechnungs- und Angebotssoftware

### Entwickelt für Baum Performance Stahl

> Lokale Desktop-Anwendung zur Erstellung, Verwaltung und Archivierung von Angeboten, Rechnungen und Belegen.

**Status:** Aktive Entwicklung

---

## Projektziel

Billory digitalisiert die bisher manuelle Verwaltung von Angeboten, Rechnungen und Belegen. Die Anwendung richtet sich an kleine Unternehmen und Einzelunternehmer, die ihre Dokumente lokal und ohne Cloud-Abhängigkeit verwalten möchten.

---

## Aktuell implementierte Funktionen

### Authentifizierung

- Passwortgeschützter Login
- Passwort-Hashing mit BCrypt
- Lokale Benutzerverwaltung

### Kundenverwaltung

- Kunden anlegen, bearbeiten und löschen
- Live-Suche
- Validierung und Fehlerbehandlung

### Angebote & Rechnungen

- Angebote erstellen
- Rechnungen erstellen
- Angebote in Rechnungen umwandeln
- Automatische Rechnungsnummerierung
- Dokumentstatus:
  - Entwurf
  - Offen
  - Bezahlt
  - Storniert
- Historische Rechnungen erfassen

### PDF-Erzeugung

- PDF-Erstellung für Angebote und Rechnungen
- Mahnungserstellung
- Speicherung im konfigurierbaren Archivordner

### Externe Belege

- Verwaltung externer Belege
- Jahresübersicht
- Bearbeiten und Löschen von Belegen
- Vorbereitung für CSV-Export

### Einstellungen

- Firmenstammdaten
- Bankverbindung
- Archivpfade
- Belegpfade
- Backup-Pfade
- Mahnungsvorlagen
- Rechtstexte

---

## Technologie-Stack

| Bereich | Technologie |
|----------|-------------|
| Desktop | Electron |
| Frontend | React 19 + TypeScript |
| Backend | Spring Boot 3 |
| Sprache | Java 21 |
| Datenbank | SQLite |
| ORM | Spring Data JPA / Hibernate |
| Migrationen | Flyway |
| Sicherheit | Spring Security + BCrypt |
| Build | Maven |
| Packaging | Electron Builder |

---

## Projektstruktur

```text
billory/
├── backend/                  # Spring Boot Backend
│   ├── auth/
│   ├── customer/
│   ├── document/
│   ├── externalinvoice/
│   ├── pdf/
│   └── settings/
│
├── docs/                     # Lastenheft, Pflichtenheft, Architektur
│
├── src/
│   ├── main/                 # Electron Main Process
│   └── renderer/
│       ├── api/
│       ├── layouts/
│       ├── pages/
│       └── types/
│
└── README.md

---

## Entwicklung starten

### Backend

```bash
cd backend
mvn spring-boot:run
```

Backend läuft anschließend auf:

```text
http://localhost:8080
```

### Frontend / Electron

```bash
npm install
npm run dev
```

---

## Roadmap

- [x] Authentifizierung
- [x] Kundenverwaltung
- [x] Dokumentverwaltung
- [x] PDF-Erzeugung
- [x] Historische Rechnungen
- [x] Externe Belege
- [ ] CSV-Export
- [ ] Dashboard-Kennzahlen
- [ ] Dateiauswahl für PDFs
- [ ] Windows-Installer
- [ ] Produktivtests

---

## Nicht-Ziele

- Cloud-Synchronisation
- Mehrbenutzerbetrieb
- Vollständige Buchhaltungssoftware
- Online-Zahlungsabwicklung
- OCR-Dokumentenerkennung

---

## Autor

**Daniel Schitz**

Softwareentwickler

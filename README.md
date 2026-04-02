# Billory – Rechnungs- und Angebotssoftware
### Entwickelt für Baum Performance Stahl 

> Lokale Desktop-Anwendung zur digitalen Erstellung, Verwaltung und Archivierung von Angeboten und Rechnungen – ohne Cloud, ohne Abo, ohne Internetzwischen.

**Status:** Work in Progress – nicht für Produktion geeignet

---

## Hintergrund

Baum Performance Stahl erstellt Angebote und Rechnungen bisher manuell in Excel und archiviert diese teilweise in Papierform. Ziel dieses Projekts ist eine schlanke Desktop-Anwendung, die den gesamten Dokumentenprozess digitalisiert und vereinfacht – von der Angebotserstellung bis zur CSV-Übergabe an den Steuerberater.

---

## Features (geplant / im Aufbau)

- Angebote erstellen und als PDF speichern
- Angebote per Klick in Rechnungen umwandeln
- Automatische Rechnungsnummerierung (Format `FNMMYY`, z. B. `F020325`)
- Kundenverwaltung mit Stammdaten und Live-Suche
- Zahlungsstatus je Rechnung (offen / bezahlt)
- Übersicht offener Rechnungen und ausstehender Beträge
- Mahnungserstellung aus Vorlage
- Verwaltung externer Belege (Betriebsausgaben) mit Vorsteuerberechnung
- CSV-Export aller Rechnungen zur Übergabe an den Steuerberater
- Passwortschutz beim Anwendungsstart
- Automatisches Datenbank-Backup beim Start

---

## Architektur & Technologien

| Bereich | Technologie | Begründung |
|---|---|---|
| Desktop-Framework | Electron.js | Native Windows-App auf Basis von Web-Technologien – kein Java, keine externe Laufzeitumgebung nötig |
| Frontend | React + TypeScript | Komponentenbasierte UI, statische Typisierung reduziert Laufzeitfehler |
| Datenbank | SQLite | Dateibasiert, keine Installation, einfaches Backup durch Kopieren der Datei |
| PDF-Erzeugung | PDFKit | Node.js-nativ, keine externe Abhängigkeit |
| Packaging | Electron Builder + NSIS | Erzeugt Windows-Installer (.exe) mit Startmenü-Eintrag |

---

## Installation & Start

**Voraussetzung**
- Windows 10 oder Windows 11 (64-Bit)
- Keine weitere Software erforderlich

**Start (geplant)**
1. Installer `billory-setup.exe` ausführen
2. Anwendung über Startmenü starten
3. Beim ersten Start: Passwort vergeben und Firmenstammdaten eingeben
4. Loslegen

---

## Projektstruktur

```
billory/
│
├── docs/                          # Projektdokumentation
│   ├── 01_lastenheft.md
│   ├── 02_pflichtenheft.md
│   ├── 03_datenbank-architektur.md
│   ├── 04_api-design.md
│   └── 05_architekturdiagramme.md
│
├── src/
│   ├── main/                      # Electron Main Process (Node.js)
│   │   ├── ipc/                   # IPC Handler
│   │   ├── services/              # Geschäftslogik
│   │   ├── repositories/          # Datenbankzugriffe
│   │   └── migrations/            # Datenbankmigrationen
│   │
│   └── renderer/                  # React Frontend
│       ├── components/            # Wiederverwendbare UI-Komponenten
│       ├── pages/                 # Seiten (Dashboard, Rechnungen, Kunden …)
│       └── context/               # State Management
│
└── assets/                        # Logo, Icons
```

---

## Roadmap

- [ ] Projekt-Setup (Electron + React + TypeScript + SQLite)
- [ ] Datenbankschema und Migrations-System
- [ ] Authentifizierung (Passwortschutz)
- [ ] Kundenverwaltung
- [ ] Angebots- und Rechnungserstellung
- [ ] PDF-Generierung
- [ ] Externe Belegverwaltung
- [ ] CSV-Export
- [ ] Windows-Installer
- [ ] Abnahmetests beim Kunden

---

## Nicht-Ziele

Folgende Funktionen sind bewusst nicht Bestandteil des Projekts:

- Cloud-Speicherung oder Mehrbenutzerbetrieb
- Vollständige Buchhaltungssoftware
- Online-Zahlungsabwicklung
- Automatische Verarbeitung eingescannter Dokumente

---

## Dokumentation

Die vollständige Projektdokumentation (Lastenheft, Pflichtenheft, Datenbankarchitektur, API-Design, Architekturdiagramme) befindet sich im [`docs/`](./docs) Ordner.

---

## Autor

**Daniel Schitz**
Alleiniger Entwickler

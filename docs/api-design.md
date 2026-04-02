# API-Design – IPC-Kanäle
## Rechnungs- und Angebotssoftware – Baum Performance Stahl

Kommunikation zwischen React-Frontend und Electron-Backend via `ipcRenderer.invoke()`.

Alle Kanäle geben zurück:
```typescript
{ ok: true, data: T } | { ok: false, error: string }
```

---

## Authentifizierung

| Kanal | Request | Response |
|---|---|---|
| `auth:verify` | `{ password }` | `{ success }` |
| `auth:changePassword` | `{ currentPassword, newPassword }` | `{ success }` |

---

## Einstellungen

| Kanal | Request | Response |
|---|---|---|
| `settings:get` | — | `Settings` |
| `settings:update` | `Partial<Settings>` | `{ success }` |

---

## Kunden

| Kanal | Request | Response |
|---|---|---|
| `customers:getAll` | `{ search? }` | `Customer[]` |
| `customers:getById` | `{ id }` | `Customer` |
| `customers:create` | `{ name, street, zip, city, email?, phone?, notes? }` | `{ id }` |
| `customers:update` | `{ id, ...felder }` | `{ success }` |
| `customers:delete` | `{ id }` | `{ success }` |

---

## Dokumente

| Kanal | Request | Response |
|---|---|---|
| `documents:getAll` | `{ type?, status? }` | `DocumentSummary[]` |
| `documents:getById` | `{ id }` | `DocumentDetail` |
| `documents:create` | `{ type, customerId, documentDate, serviceDate?, validUntil?, notes?, lineItems[], isHistorical? }` | `{ id, invoiceNumber? }` |
| `documents:convertToInvoice` | `{ offerId }` | `{ id, invoiceNumber }` |
| `documents:updateStatus` | `{ id, status }` | `{ success }` |
| `documents:delete` | `{ id }` | `{ success }` |

---

## Positionen

| Kanal | Request | Response |
|---|---|---|
| `lineItems:update` | `{ documentId, lineItems[] }` | `{ success }` |

---

## Externe Belege

| Kanal | Request | Response |
|---|---|---|
| `externalInvoices:getByYear` | `{ year }` | `ExternalInvoice[]` |
| `externalInvoices:getYearlySummary` | `{ year }` | `{ totalGross, totalNet, totalTax }` |
| `externalInvoices:scanFolder` | `{ year }` | `{ fileName, filePath, alreadySaved }[]` |
| `externalInvoices:create` | `{ filePath, year, date, description, category?, grossAmount }` | `{ id }` |
| `externalInvoices:update` | `{ id, ...felder }` | `{ success }` |
| `externalInvoices:delete` | `{ id }` | `{ success }` |

---

## PDF

| Kanal | Request | Response |
|---|---|---|
| `pdf:generate` | `{ documentId }` | `{ filePath }` |
| `pdf:generateReminder` | `{ invoiceId }` | `{ filePath }` |
| `pdf:open` | `{ filePath }` | `{ success }` |

---

## Export

| Kanal | Request | Response |
|---|---|---|
| `export:csv` | `{ dateFrom?, dateTo? }` | `{ filePath }` |
| `export:externalInvoicesCsv` | `{ year }` | `{ filePath }` |

---

## Typen

```typescript
type DocumentType   = 'offer' | 'invoice'
type DocumentStatus = 'draft' | 'open' | 'paid' | 'cancelled'

interface LineItemInput {
  position:    number
  description: string
  grossAmount: number
}

interface DocumentSummary {
  id, type, status, invoiceNumber, documentDate, grossTotal, netTotal, taxTotal, pdfPath
  customer: { id, name }
}

interface DocumentDetail extends DocumentSummary {
  customer:    Customer
  serviceDate, validUntil, notes
  lineItems:   LineItem[]
}
```

import { useEffect, useState } from 'react'

import { documentApi } from '../api/documentApi'

import { pdfApi } from '../api/pdfApi'

import type {
  DocumentStatus,
  DocumentSummary,
  DocumentType,
} from '../types/api'

function formatDocumentType(type: string) {
  return type === 'INVOICE' ? 'Rechnung' : 'Angebot'
}

function formatDocumentStatus(status: string) {
  switch (status) {
    case 'DRAFT':
      return 'Entwurf'
    case 'OPEN':
      return 'Offen'
    case 'PAID':
      return 'Bezahlt'
    case 'CANCELLED':
      return 'Storniert'
    default:
      return status
  }
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}

export default function DocumentsPage() {
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [typeFilter, setTypeFilter] = useState<DocumentType | ''>('')
  const [statusFilter, setStatusFilter] = useState<DocumentStatus | ''>('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function loadDocuments() {
    setIsLoading(true)
    setError('')

    try {
      const data = await documentApi.getAll({
        type: typeFilter,
        status: statusFilter,
      })

      setDocuments(data)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Dokumente konnten nicht geladen werden.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadDocuments()
  }, [typeFilter, statusFilter])

  return (
    <main>
      <h2>Dokumente</h2>

      <div>
        <select
          value={typeFilter}
          onChange={(event) =>
            setTypeFilter(event.target.value as DocumentType | '')
          }
        >
          <option value="">Alle Typen</option>
          <option value="INVOICE">Rechnungen</option>
          <option value="OFFER">Angebote</option>
        </select>

        <select
          value={statusFilter}
          onChange={(event) =>
            setStatusFilter(event.target.value as DocumentStatus | '')
          }
        >
          <option value="">Alle Status</option>
          <option value="DRAFT">Entwurf</option>
          <option value="OPEN">Offen</option>
          <option value="PAID">Bezahlt</option>
          <option value="CANCELLED">Storniert</option>
        </select>

        <button type="button" onClick={loadDocuments}>
          Aktualisieren
        </button>
      </div>

      {isLoading && <p>Lade Dokumente...</p>}
      {error && <p>{error}</p>}

      <p>
        {documents.length} Dokument(e) gefunden
      </p>

      {!isLoading && (
        <table>
          <thead>
            <tr>
              <th>Typ</th>
              <th>Status</th>
              <th>Nummer</th>
              <th>Kunde</th>
              <th>Datum</th>
              <th>Brutto</th>
              <th>Aktionen</th>
            </tr>
          </thead>

          <tbody>

            {documents.length === 0 && (
              <tr>
                <td colSpan={7}>
                  Keine Dokumente gefunden.
                </td>
              </tr>
            )}
            
            {documents.map((document) => (
              <tr key={document.id}>
                <td>{formatDocumentType(document.type)}</td>
                <td>{formatDocumentStatus(document.status)}</td>
                <td>{document.invoiceNumber || '-'}</td>
                <td>{document.customerName}</td>
                <td>{document.documentDate}</td>
                <td>{formatCurrency(document.grossTotal)}</td>
                <td>
                  <button
                    type="button"
                    onClick={async () => {
                      try {
                        const filePath =
                          await pdfApi.generateDocument(document.id)

                        alert(`PDF erzeugt:\n${filePath.filePath}`)

                        await loadDocuments()
                      } catch (error) {
                        setError(
                          error instanceof Error
                            ? error.message
                            : 'PDF konnte nicht erzeugt werden.'
                        )
                      }
                    }}
                  >
                    PDF erzeugen
                  </button>

                  {document.type === 'INVOICE' && document.status === 'OPEN' && (
                  <button
                    type="button"
                    onClick={async () => {
                      try {
                        const filePath =
                          await pdfApi.generateReminder(document.id)

                        alert(`Mahnung erzeugt:\n${filePath.filePath}`)
                      } catch (error) {
                        setError(
                          error instanceof Error
                            ? error.message
                            : 'Mahnung konnte nicht erzeugt werden.'
                        )
                      }
                    }}
                    >
                      Mahnung erzeugen
                    </button>
                  )}

                  {document.type === 'INVOICE' && document.status === 'OPEN' && (
                    <button
                      type="button"
                      onClick={async () => {
                        try {
                          await documentApi.updateStatus(document.id, {
                            status: 'PAID',
                          })

                          await loadDocuments()
                        } catch (error) {
                          setError(
                            error instanceof Error
                              ? error.message
                              : 'Status konnte nicht geändert werden.'
                          )
                        }
                      }}
                    >
                      Als bezahlt markieren
                    </button>
                  )}

                  {document.type === 'OFFER' && (
                    <button
                      type="button"
                      onClick={async () => {
                        try {
                          const createdInvoice =
                            await documentApi.convertToInvoice({
                              offerId: document.id,
                            })

                          alert(
                            `Rechnung erstellt: ${createdInvoice.invoiceNumber || createdInvoice.id}`
                          )

                          await loadDocuments()
                        } catch (error) {
                          setError(
                            error instanceof Error
                              ? error.message
                              : 'Angebot konnte nicht konvertiert werden.'
                          )
                        }
                      }}
                    >
                      Zu Rechnung konvertieren
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}
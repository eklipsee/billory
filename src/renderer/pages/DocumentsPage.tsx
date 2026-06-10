import { useEffect, useState } from 'react'

import { documentApi } from '../api/documentApi'

import { pdfApi } from '../api/pdfApi'

import type { AppPage } from '../types/navigation'

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

type DocumentsPageProps = {
  onNavigate?: (page: AppPage) => void
}

export default function DocumentsPage({ onNavigate }: DocumentsPageProps) {
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [typeFilter, setTypeFilter] = useState<DocumentType | ''>('')
  const [statusFilter, setStatusFilter] = useState<DocumentStatus | ''>('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [openActionMenuId, setOpenActionMenuId] = useState<number | null>(null)

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
      <div className="page-header">
        <div>
          <h2>Dokumente</h2>

          <p className="page-subtitle">
            Verwalten Sie Angebote, Rechnungen und Mahnungen.
          </p>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={() => onNavigate?.('createDocument')}
        >
          + Dokument erstellen
        </button>
      </div>

      <div className="filter-card document-filter-card">
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
      {error && <p className="error">{error}</p>}
      {successMessage && <p>{successMessage}</p>}

      <p>{documents.length} Dokument(e) gefunden</p>

      {!isLoading && (
        <table>
          <thead>
            <tr>
              <th>Typ</th>
              <th>Status</th>
              <th>Historisch</th>
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
                <td colSpan={8}>Keine Dokumente gefunden.</td>
              </tr>
            )}

            {documents.map((document) => {
              const isConvertedOffer =
                document.type === 'OFFER' &&
                documents.some(
                  (otherDocument) =>
                    otherDocument.type === 'INVOICE' &&
                    otherDocument.convertedFromId === document.id
                )

              return (
                <tr key={document.id}>
                  <td>{formatDocumentType(document.type)}</td>
                  <td>
                    <span className={`status-badge status-${document.status.toLowerCase()}`}>
                      {formatDocumentStatus(document.status)}
                    </span>
                  </td>
                  <td>{document.isHistorical ? 'Ja' : 'Nein'}</td>
                  <td>{document.invoiceNumber || '-'}</td>
                  <td>{document.customerName}</td>
                  <td>{document.documentDate}</td>
                  <td>{formatCurrency(document.grossTotal)}</td>
                  <td className="document-actions">
                    <div className="action-menu-wrapper">
                      <button
                        type="button"
                        className="action-menu-button"
                        onClick={() =>
                          setOpenActionMenuId(
                            openActionMenuId === document.id ? null : document.id
                          )
                        }
                      >
                        ⋮
                      </button>

                      {openActionMenuId === document.id && (
                        <div className="action-menu">
                          <button
                            type="button"
                            onClick={async () => {
                              setOpenActionMenuId(null)

                              try {
                                setError('')
                                setSuccessMessage('')

                                const filePath = await pdfApi.generateDocument(document.id)

                                setSuccessMessage(`PDF erzeugt: ${filePath.filePath}`)

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
                            <>
                              <button
                                type="button"
                                onClick={async () => {
                                  setOpenActionMenuId(null)

                                  try {
                                    setError('')
                                    setSuccessMessage('')

                                    const filePath = await pdfApi.generateReminder(document.id)

                                    setSuccessMessage(`Mahnung erzeugt: ${filePath.filePath}`)
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

                              <button
                                type="button"
                                onClick={async () => {
                                  setOpenActionMenuId(null)

                                  try {
                                    setError('')
                                    setSuccessMessage('')

                                    await documentApi.updateStatus(document.id, {
                                      status: 'PAID',
                                    })

                                    setSuccessMessage('Rechnung als bezahlt markiert.')

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
                            </>
                          )}

                          {document.type === 'OFFER' && !isConvertedOffer && (
                            <button
                              type="button"
                              onClick={async () => {
                                setOpenActionMenuId(null)

                                try {
                                  setError('')
                                  setSuccessMessage('')

                                  const createdInvoice = await documentApi.convertToInvoice({
                                    offerId: document.id,
                                  })

                                  setSuccessMessage(
                                    `Rechnung erstellt: ${
                                      createdInvoice.invoiceNumber || createdInvoice.id
                                    }`
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

                          {document.type === 'OFFER' && isConvertedOffer && (
                            <div className="action-menu-info">
                              Bereits konvertiert
                            </div>
                          )}

                          {document.type === 'OFFER' && (
                            <button
                              type="button"
                              className="danger-menu-item"
                              onClick={async () => {
                                setOpenActionMenuId(null)

                                const confirmed = window.confirm(
                                  'Dieses Angebot wirklich löschen?'
                                )

                                if (!confirmed) {
                                  return
                                }

                                try {
                                  setError('')
                                  setSuccessMessage('')

                                  await documentApi.delete(document.id)
                                  setSuccessMessage('Angebot wurde gelöscht.')
                                  await loadDocuments()
                                } catch (error) {
                                  setError(
                                    error instanceof Error
                                      ? error.message
                                      : 'Angebot konnte nicht gelöscht werden.'
                                  )
                                }
                              }}
                            >
                              Angebot löschen
                            </button>
                          )}

                          {document.isHistorical && (
                            <button
                              type="button"
                              onClick={async () => {
                                setOpenActionMenuId(null)

                                const sourceFilePath = 'C:/Users/Daniel/Desktop/test.pdf'

                                try {
                                  setError('')
                                  setSuccessMessage('')

                                  await documentApi.attachPdf(document.id, {
                                    sourceFilePath,
                                  })

                                  setSuccessMessage('PDF wurde angehängt.')
                                  await loadDocuments()
                                } catch (error) {
                                  setError(
                                    error instanceof Error
                                      ? error.message
                                      : 'PDF konnte nicht angehängt werden.'
                                  )
                                }
                              }}
                            >
                              PDF anhängen
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      )}
    </main>
  )
}
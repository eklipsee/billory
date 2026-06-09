import { useEffect, useState } from 'react'

import { customerApi } from '../api/customerApi'
import { documentApi } from '../api/documentApi'

import type { Customer, DocumentSummary } from '../types/api'

function formatCurrency(value: number) {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}

export default function DashboardPage() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function loadDashboardData() {
    setIsLoading(true)
    setError('')

    try {
      const [customerData, documentData] = await Promise.all([
        customerApi.getAll(),
        documentApi.getAll(),
      ])

      setCustomers(customerData)
      setDocuments(documentData)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Dashboard konnte nicht geladen werden.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadDashboardData()
  }, [])

  const openInvoices = documents.filter(
    (document) =>
      document.type === 'INVOICE' &&
      document.status === 'OPEN'
  )

  const openAmount = openInvoices.reduce(
    (sum, document) => sum + document.grossTotal,
    0
  )

  const latestDocuments = [...documents]
    .sort((a, b) =>
      b.documentDate.localeCompare(a.documentDate)
    )
    .slice(0, 5)

  return (
    <main>
      <h2>Dashboard</h2>

      <button type="button" onClick={loadDashboardData}>
        Aktualisieren
      </button>

      {isLoading && <p>Lade Dashboard...</p>}
      {error && <p className="error">{error}</p>}

      {!isLoading && !error && (
        <>
          <div className="dashboard-grid">
            <div className="dashboard-card">
              Kunden
              <strong>{customers.length}</strong>
            </div>

            <div className="dashboard-card">
              Dokumente
              <strong>{documents.length}</strong>
            </div>

            <div className="dashboard-card">
              Offene Rechnungen
              <strong>{openInvoices.length}</strong>
            </div>

            <div className="dashboard-card">
              Offener Betrag
              <strong>{formatCurrency(openAmount)}</strong>
            </div>
          </div>
          <h3>Letzte Dokumente</h3>

          <table>
            <thead>
              <tr>
                <th>Datum</th>
                <th>Typ</th>
                <th>Nummer</th>
                <th>Kunde</th>
                <th>Betrag</th>
              </tr>
            </thead>

            <tbody>
              {latestDocuments.length === 0 && (
                <tr>
                  <td colSpan={5}>Keine Dokumente vorhanden.</td>
                </tr>
              )}

              {latestDocuments.map((document) => (
                <tr key={document.id}>
                  <td>{document.documentDate}</td>
                  <td>{document.type === 'INVOICE' ? 'Rechnung' : 'Angebot'}</td>
                  <td>{document.invoiceNumber || '-'}</td>
                  <td>{document.customerName}</td>
                  <td>{formatCurrency(document.grossTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </main>
  )
}
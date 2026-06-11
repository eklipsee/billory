import { useEffect, useState } from 'react'

import { customerApi } from '../api/customerApi'
import { documentApi } from '../api/documentApi'
import { externalInvoiceApi } from '../api/externalInvoiceApi'
import type { AppPage } from '../types/navigation'

import type {
  Customer,
  DocumentSummary,
  ExternalInvoiceSummary,
} from '../types/api'

function formatCurrency(value: number) {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}

type DashboardPageProps = {
  onNavigate: (page: AppPage) => void
}

export default function DashboardPage({
  onNavigate,
}: DashboardPageProps) {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [expenseSummary, setExpenseSummary] =
    useState<ExternalInvoiceSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function loadDashboardData() {
    setIsLoading(true)
    setError('')

    try {
      const currentYear = new Date().getFullYear()

      const [customerData, documentData, expenseData] = await Promise.all([
        customerApi.getAll(),
        documentApi.getAll(),
        externalInvoiceApi.getSummary(currentYear),
      ])

      setCustomers(customerData)
      setDocuments(documentData)
      setExpenseSummary(expenseData)
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

  const today = new Date()
  const currentYear = today.getFullYear()

  const overdueInvoices = openInvoices.filter((document) => {
    const documentDate = new Date(document.documentDate)
    const ageInDays =
      (today.getTime() - documentDate.getTime()) / (1000 * 60 * 60 * 24)

    return ageInDays > 14
  })

  const openInvoicesPreview = [...openInvoices]
    .sort((a, b) => a.documentDate.localeCompare(b.documentDate))
    .slice(0, 5)

  const yearlyIncome = documents
    .filter(
      (document) =>
        document.type === 'INVOICE' &&
        document.status !== 'CANCELLED' &&
        document.documentDate.startsWith(String(currentYear))
    )
    .reduce((sum, document) => sum + document.grossTotal, 0)

  const yearlyExpenses = expenseSummary?.totalGross || 0

  return (
    <main>
      <div className="page-header">
        <div>
          <h2>Dashboard</h2>
          <p className="page-subtitle">
            Überblick über offene Rechnungen und das aktuelle Jahr.
          </p>
        </div>

        <button type="button" onClick={loadDashboardData}>
          Aktualisieren
        </button>
      </div>

      {isLoading && <p>Lade Dashboard...</p>}
      {error && <p className="error">{error}</p>}

      {!isLoading && !error && (
        <>
          <div className="dashboard-grid dashboard-kpi-grid">
            <div className="dashboard-card dashboard-kpi-card">
              <span>Offene Beträge</span>
              <strong>{formatCurrency(openAmount)}</strong>
              <small>Gesamtbetrag aus offenen Rechnungen</small>
            </div>

            <div className="dashboard-card dashboard-kpi-card">
              <span>Offene Rechnungen</span>
              <strong>{openInvoices.length}</strong>
              <small>Rechnungen noch nicht bezahlt</small>
            </div>

            <div className="dashboard-card dashboard-kpi-card">
              <span>Überfällige Rechnungen</span>
              <strong>{overdueInvoices.length}</strong>
              <small>Älter als 14 Tage</small>
            </div>
          </div>

          <section className="dashboard-section">
            <div className="dashboard-section-header">
              <h3>Offene Rechnungen</h3>
              {openInvoices.length > 5 && (
                <span>+ {openInvoices.length - 5} weitere offene Rechnungen</span>
              )}
            </div>

            <table className="dashboard-table">
              <thead>
                <tr>
                  <th>Nummer</th>
                  <th>Kunde</th>
                  <th>Datum</th>
                  <th>Betrag</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>
                {openInvoicesPreview.length === 0 && (
                  <tr>
                    <td colSpan={5}>Keine offenen Rechnungen vorhanden.</td>
                  </tr>
                )}

                {openInvoicesPreview.map((document) => (
                  <tr key={document.id}>
                    <td>{document.invoiceNumber || '-'}</td>
                    <td>{document.customerName}</td>
                    <td>{document.documentDate}</td>
                    <td>{formatCurrency(document.grossTotal)}</td>
                    <td>
                      <span className="status-badge status-open">
                        Offen
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <div className="dashboard-bottom-grid">
            <section className="dashboard-section">
              <h3>Jahresübersicht ({currentYear})</h3>

              <div className="dashboard-year-grid">
                <div className="dashboard-year-card">
                  <span>Einnahmen</span>
                  <strong>{formatCurrency(yearlyIncome)}</strong>
                </div>

                <div className="dashboard-year-card">
                  <span>Ausgaben</span>
                  <strong>{formatCurrency(yearlyExpenses)}</strong>
                </div>
              </div>
            </section>

            <section className="dashboard-section">
              <h3>Schnellaktionen</h3>

              <div className="dashboard-actions">
                <button
                  type="button"
                  onClick={() => onNavigate('createDocument')}
                >
                  + Rechnung erstellen
                </button>

                <button
                  type="button"
                  onClick={() => onNavigate('createDocument')}
                >
                  + Angebot erstellen
                </button>

                <button
                  type="button"
                  onClick={() => onNavigate('externalInvoices')}
                >
                  + Beleg erfassen
                </button>
              </div>
            </section>
          </div>
        </>
      )}
    </main>
  )
}
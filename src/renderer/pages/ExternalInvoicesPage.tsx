import { useEffect, useState } from 'react'

import { externalInvoiceApi } from '../api/externalInvoiceApi'

import type { ExternalInvoice, ExternalInvoiceSummary } from '../types/api'

function formatCurrency(value: number) {
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}

export default function ExternalInvoicesPage() {
  const [year, setYear] = useState(
    new Date().getFullYear()
  )
  const [items, setItems] = useState<ExternalInvoice[]>([])
  const [summary, setSummary] = useState<ExternalInvoiceSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [sourceFilePath, setSourceFilePath] = useState('')
  const [date, setDate] = useState('2026-05-21')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [grossAmount, setGrossAmount] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)

  async function loadExternalInvoices() {
    setIsLoading(true)
    setError('')

    try {
      const [invoiceData, summaryData] = await Promise.all([
        externalInvoiceApi.getByYear(year),
        externalInvoiceApi.getSummary(year),
      ])

      setItems(invoiceData)
      setSummary(summaryData)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Externe Belege konnten nicht geladen werden.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadExternalInvoices()
  }, [year])

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()

    setError('')

    try {
      if (editingId === null) {
        await externalInvoiceApi.create({
          sourceFilePath,
          year,
          date,
          description,
          category,
          grossAmount: Number(grossAmount),
        })
      } else {
        await externalInvoiceApi.update(editingId, {
          date,
          description,
          category,
          grossAmount: Number(grossAmount),
        })
      }

      setSourceFilePath('')
      setDescription('')
      setCategory('')
      setGrossAmount('')

      setEditingId(null)

      await loadExternalInvoices()
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Beleg konnte nicht erstellt werden.'
      )
    }
  }

  async function handleSelectPdf() {
    if (!window.electronAPI) {
      setError('Electron API nicht verfügbar.')
      return
    }

    const selectedFile =
      await window.electronAPI.selectPdfFile()

    if (selectedFile) {
      setSourceFilePath(selectedFile)
    }
  }

  return (
    <main>
      <div className="page-header">
        <div>
          <h2>Belege</h2>
          <p className="page-subtitle">
            Verwalten Sie Ihre externen Belege und Kosten.
          </p>
        </div>
      </div>

      {summary && (
        <div className="dashboard-grid">
          <div className="dashboard-card">
            <div>Netto gesamt</div>
            <strong>{formatCurrency(summary.totalNet)}</strong>
          </div>

          <div className="dashboard-card">
            <div>Brutto gesamt</div>
            <strong>{formatCurrency(summary.totalGross)}</strong>
          </div>

          <div className="dashboard-card">
            <div>Steuern gesamt</div>
            <strong>{formatCurrency(summary.totalTax)}</strong>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="receipt-form-card">
        <div className="form-header">
          <h3>{editingId === null ? 'Neuen Beleg erfassen' : 'Beleg bearbeiten'}</h3>
        </div>

        <div className="receipt-file-row">
          <input
            type="text"
            value={sourceFilePath}
            readOnly
            placeholder="Keine PDF ausgewählt"
          />

          <button type="button" onClick={handleSelectPdf}>
            PDF auswählen
          </button>
        </div>

        <div className="receipt-form-grid">
          <input
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
          />

          <input
            type="text"
            placeholder="Beschreibung"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />

          <input
            type="text"
            placeholder="Kategorie"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
          />

          <input
            type="number"
            placeholder="Brutto"
            value={grossAmount}
            onChange={(event) => setGrossAmount(event.target.value)}
          />
        </div>

        <div className="form-actions">
          {editingId !== null && (
            <button
              type="button"
              onClick={() => {
                setEditingId(null)
                setSourceFilePath('')
                setDescription('')
                setCategory('')
                setGrossAmount('')
              }}
            >
              Abbrechen
            </button>
          )}

          <button type="submit" className="primary-button">
            {editingId === null ? 'Beleg speichern' : 'Änderungen speichern'}
          </button>
        </div>
      </form>

      <div className="filter-card receipt-year-card">
        <label>
          Jahr
          <select
            value={year}
            onChange={(event) => setYear(Number(event.target.value))}
          >
            {Array.from({ length: 6 }, (_, index) => {
              const optionYear = new Date().getFullYear() - index

              return (
                <option key={optionYear} value={optionYear}>
                  {optionYear}
                </option>
              )
            })}
          </select>
        </label>

        <button type="button" onClick={loadExternalInvoices}>
          Aktualisieren
        </button>

        <button
          type="button"
          onClick={async () => {
            if (!window.electronAPI) {
              setError('Electron API nicht verfügbar.')
              return
            }

            await window.electronAPI.openBilloryFolder('Belege')
          }}
        >
          Belegordner öffnen
        </button>
      </div>

      {isLoading && <p>Lade Belege...</p>}
      {error && <p className="error">{error}</p>}

      

      <table className="receipt-table">
        <thead>
          <tr>
            <th>Datum</th>
            <th>Beschreibung</th>
            <th>Kategorie</th>
            <th>Netto</th>
            <th>Brutto</th>
            <th>Steuer</th>
            <th>Aktionen</th>
          </tr>
        </thead>

        <tbody>
          {items.length === 0 && (
            <tr>
              <td colSpan={7}>Keine Belege für dieses Jahr gefunden.</td>
            </tr>
          )}

          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.date}</td>
              <td>{item.description}</td>
              <td>{item.category || '-'}</td>
              <td>{formatCurrency(item.netAmount)}</td>
              <td>{formatCurrency(item.grossAmount)}</td>
              <td>{formatCurrency(item.taxAmount)}</td>
              <td>
                <div className="table-actions">
                  <button
                    type="button"
                    className="icon-action"
                    title="PDF öffnen"
                    onClick={async () => {
                      if (!window.electronAPI) {
                        setError('Electron API nicht verfügbar.')
                        return
                      }

                      await window.electronAPI.openFile(item.filePath)
                    }}
                  >
                    📄
                  </button>

                  <button
                    type="button"
                    className="icon-action edit-action"
                    title="Bearbeiten"
                    onClick={() => {
                      setEditingId(item.id)
                      setSourceFilePath(item.filePath)
                      setDate(item.date)
                      setDescription(item.description)
                      setCategory(item.category || '')
                      setGrossAmount(String(item.grossAmount))

                      window.scrollTo({
                        top: 0,
                        behavior: 'smooth',
                      })
                    }}
                  >
                    ✎
                  </button>

                  <button
                    type="button"
                    className="icon-action delete-action"
                    title="Löschen"
                    onClick={async () => {
                      const confirmed = window.confirm(
                        `Beleg "${item.description}" wirklich löschen?`
                      )

                      if (!confirmed) {
                        return
                      }

                      try {
                        await externalInvoiceApi.delete(item.id)
                        await loadExternalInvoices()
                      } catch (error) {
                        setError(
                          error instanceof Error
                            ? error.message
                            : 'Beleg konnte nicht gelöscht werden.'
                        )
                      }
                    }}
                  >
                    🗑
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}
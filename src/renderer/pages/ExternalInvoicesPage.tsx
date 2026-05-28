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

  return (
    <main>
      <h2>Externe Belege</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="PDF Pfad"
          value={sourceFilePath}
          onChange={(event) =>
            setSourceFilePath(event.target.value)
          }
        />

        <input
          type="date"
          value={date}
          onChange={(event) => setDate(event.target.value)}
        />

        <input
          type="text"
          placeholder="Beschreibung"
          value={description}
          onChange={(event) =>
            setDescription(event.target.value)
          }
        />

        <input
          type="text"
          placeholder="Kategorie"
          value={category}
          onChange={(event) =>
            setCategory(event.target.value)
          }
        />

        <input
          type="number"
          placeholder="Brutto"
          value={grossAmount}
          onChange={(event) =>
            setGrossAmount(event.target.value)
          }
        />

        <button type="submit">
          {editingId === null ? 'Beleg speichern' : 'Änderungen speichern'}
        </button>

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
      </form>

      <input
        type="number"
        value={year}
        onChange={(event) => setYear(Number(event.target.value))}
      />

      <button type="button" onClick={loadExternalInvoices}>
        Aktualisieren
      </button>

      {isLoading && <p>Lade Belege...</p>}
      {error && <p>{error}</p>}

      {summary && (
        <p>
          Brutto: {formatCurrency(summary.totalGross)} | Netto:{' '}
          {formatCurrency(summary.totalNet)} | Steuer:{' '}
          {formatCurrency(summary.totalTax)}
        </p>
      )}

      <table>
        <thead>
          <tr>
            <th>Datum</th>
            <th>Beschreibung</th>
            <th>Kategorie</th>
            <th>Brutto</th>
            <th>Datei</th>
            <th>Aktionen</th>
          </tr>
        </thead>

        <tbody>
          {items.length === 0 && (
            <tr>
              <td colSpan={6}>Keine Belege gefunden.</td>
            </tr>
          )}

          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.date}</td>
              <td>{item.description}</td>
              <td>{item.category || '-'}</td>
              <td>{formatCurrency(item.grossAmount)}</td>
              <td>{item.filePath}</td>
              <td>
                <button
                  type="button"
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
                  Löschen
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditingId(item.id)
                    setSourceFilePath(item.filePath)
                    setDate(item.date)
                    setDescription(item.description)
                    setCategory(item.category || '')
                    setGrossAmount(String(item.grossAmount))
                  }}
                >
                  Bearbeiten
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}
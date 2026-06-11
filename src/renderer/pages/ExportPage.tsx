import { useState } from 'react'

import { exportApi } from '../api/exportApi'

export default function ExportPage() {
  const [year, setYear] = useState(new Date().getFullYear())
  const [csvContent, setCsvContent] = useState('')
  const [error, setError] = useState('')

  async function handleExport() {
    setError('')
    setCsvContent('')

    try {
      const csv = await exportApi.exportCsv(year)
      setCsvContent(csv)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'CSV-Export fehlgeschlagen.'
      )
    }
  }

  function downloadCsv() {
    const blob = new Blob([csvContent], {
      type: 'text/csv;charset=utf-8;',
    })

    const url = URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    link.download = `billory-export-${year}.csv`
    link.click()

    URL.revokeObjectURL(url)
  }

  return (
    <main>
      <div className="page-header">
        <div>
          <h2>Export</h2>
          <p className="page-subtitle">
            Erstellen Sie eine Buchungsübersicht für Ihren Steuerberater.
          </p>
        </div>
      </div>

      <div className="export-grid">
        <section className="export-card">
          <h3>Zeitraum</h3>

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
        </section>

        <section className="export-card">
          <h3>Hinweis</h3>

          <p>
            Der Export enthält alle Rechnungen und Belege des ausgewählten Jahres
            im CSV-Format.
          </p>

          <p>
            PDF-Belege und Rechnungen müssen bei Bedarf separat weitergegeben werden.
          </p>
        </section>
      </div>

      <section className="export-card export-action-card">
        {!csvContent ? (
          <>
            <h3>Export</h3>

            <p>
              Erzeuge eine Buchungsübersicht als CSV-Datei für das ausgewählte Jahr.
            </p>

            <div className="form-actions">
              <button type="button" className="primary-button" onClick={handleExport}>
                CSV erzeugen
              </button>
            </div>
          </>
        ) : (
          <>
            <h3>Export erfolgreich erstellt</h3>

            <p>
              Die Buchungsübersicht für das Jahr {year} wurde erfolgreich erstellt.
            </p>

            <div className="export-file-box">
              billory-export-{year}.csv
            </div>

            <div className="form-actions">
              <button type="button" className="primary-button" onClick={downloadCsv}>
                CSV herunterladen
              </button>
            </div>
          </>
        )}
      </section>

      {error && <p className="error">{error}</p>}
      
    </main>
  )
}
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
      <h2>Export</h2>

      <input
        type="number"
        value={year}
        onChange={(event) => setYear(Number(event.target.value))}
      />

      <button type="button" onClick={handleExport}>
        CSV erzeugen
      </button>

      {csvContent && (
        <button type="button" onClick={downloadCsv}>
          CSV herunterladen
        </button>
      )}

      {error && <p>{error}</p>}

      {csvContent && (
        <div>
          <h3>CSV-Vorschau</h3>

          <pre>
            {csvContent}
          </pre>
        </div>
      )}
    </main>
  )
}
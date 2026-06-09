import { apiClient } from './apiClient'

export const exportApi = {
  async exportCsv(year: number) {
    const response = await fetch(
      `http://localhost:8080/api/export/csv?year=${year}`
    )

    if (!response.ok) {
      throw new Error('CSV-Export fehlgeschlagen.')
    }

    return response.text()
  },
}
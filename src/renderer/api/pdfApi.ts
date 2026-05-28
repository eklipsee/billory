import { apiClient } from './apiClient'
import type { PdfPathResponse } from '../types/api'

export const pdfApi = {
  generateDocument(documentId: number) {
    return apiClient.get<PdfPathResponse>(
      `/pdf/document/${documentId}`
    )
  },

  async generateReminder(invoiceId: number) {
        const response = await fetch(
            `http://localhost:8080/api/pdf/reminder/${invoiceId}`
        )

        if (!response.ok) {
            throw new Error('Mahnung konnte nicht erzeugt werden.')
        }

        const filePath = await response.text()

        return {
            filePath,
        }
    },
}
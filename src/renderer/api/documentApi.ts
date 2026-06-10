import { apiClient } from './apiClient'
import type {
  ConvertToInvoiceRequest,
  DocumentCreateRequest,
  DocumentStatus,
  DocumentSummary,
  DocumentType,
  UpdateDocumentStatusRequest,
  AttachPdfRequest,
  HistoricalDocumentCreateRequest,
} from '../types/api'

type GetDocumentsFilters = {
  type?: DocumentType | ''
  status?: DocumentStatus | ''
}

export const documentApi = {
  getAll(filters: GetDocumentsFilters = {}) {
    const params = new URLSearchParams()

    if (filters.type) {
      params.set('type', filters.type)
    }

    if (filters.status) {
      params.set('status', filters.status)
    }

    const query = params.toString()
      ? `?${params.toString()}`
      : ''

    return apiClient.get<DocumentSummary[]>(`/documents${query}`)
  },

  create(data: DocumentCreateRequest) {
    return apiClient.post<DocumentSummary>('/documents', data)
  },

  updateStatus(id: number, data: UpdateDocumentStatusRequest) {
    return apiClient.put<DocumentSummary>(
      `/documents/${id}/status`,
      data
    )
  },

  convertToInvoice(data: ConvertToInvoiceRequest) {
    return apiClient.put<DocumentSummary>(
      '/documents/convert-to-invoice',
      data
    )
  },

  createHistorical(
    data: HistoricalDocumentCreateRequest
  ) {
    return apiClient.post<DocumentSummary>(
      '/documents/historical',
      data
    )
  },

  attachPdf(
    id: number,
    data: AttachPdfRequest
  ) {
    return apiClient.post<DocumentSummary>(
      `/documents/${id}/attach-pdf`,
      data
    )
  },

  delete(id: number) {
    return apiClient.delete<void>(
      `/documents/${id}`
    )
  },
}
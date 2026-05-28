import { apiClient } from './apiClient'
import type {
  DocumentCreateRequest,
  DocumentStatus,
  DocumentSummary,
  DocumentType,
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
}
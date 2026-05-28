import { apiClient } from './apiClient'

import type {
  ExternalInvoice,
  ExternalInvoiceCreateRequest,
  ExternalInvoiceSummary,
  ExternalInvoiceUpdateRequest,
} from '../types/api'

export const externalInvoiceApi = {
  getByYear(year: number) {
    return apiClient.get<ExternalInvoice[]>(
      `/external-invoices?year=${year}`
    )
  },

  getSummary(year: number) {
    return apiClient.get<ExternalInvoiceSummary>(
      `/external-invoices/summary?year=${year}`
    )
  },

  create(data: ExternalInvoiceCreateRequest) {
    return apiClient.post<ExternalInvoice>(
      '/external-invoices',
      data
    )
  },

  update(id: number, data: ExternalInvoiceUpdateRequest) {
    return apiClient.put<ExternalInvoice>(
      `/external-invoices/${id}`,
      data
    )
  },

  delete(id: number) {
    return apiClient.delete<void>(
      `/external-invoices/${id}`
    )
  },
}
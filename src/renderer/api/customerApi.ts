import { apiClient } from './apiClient'
import type {
  Customer,
  CustomerCreateRequest,
  CustomerUpdateRequest,
} from '../types/api'

export const customerApi = {
  getAll(search?: string) {
    const query = search
      ? `?search=${encodeURIComponent(search)}`
      : ''

    return apiClient.get<Customer[]>(`/customers${query}`)
  },

  create(data: CustomerCreateRequest) {
    return apiClient.post<Customer>('/customers', data)
  },

  update(id: number, data: CustomerUpdateRequest) {
    return apiClient.put<Customer>(`/customers/${id}`, data)
  },

  delete(id: number) {
    return apiClient.delete<void>(`/customers/${id}`)
  },
}
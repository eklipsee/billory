import { apiClient } from './apiClient'

import type {
  ChangePasswordRequest,
  Settings,
  SettingsUpdateRequest,
} from '../types/api'

export const settingsApi = {
  get() {
    return apiClient.get<Settings>('/settings')
  },

  update(data: SettingsUpdateRequest) {
    return apiClient.put<Settings>('/settings', data)
  },

  changePassword(data: ChangePasswordRequest) {
    return apiClient.post<{ success: boolean }>(
      '/auth/change-password',
      data
    )
  },
}
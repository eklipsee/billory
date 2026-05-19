import { apiClient } from './apiClient'
import type {
  AuthLoginRequest,
  AuthLoginResponse,
} from '../types/api'

export const authApi = {
  login(data: AuthLoginRequest) {
    return apiClient.post<AuthLoginResponse>(
      '/auth/login',
      data
    )
  },
}
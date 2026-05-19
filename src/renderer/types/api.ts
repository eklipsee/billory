export type AuthLoginRequest = {
  password: string
}

export type AuthLoginResponse = {
  success: boolean
}

export type ApiValidationErrors = Record<string, string>

export type ApiError = {
  message: string
  errors?: ApiValidationErrors | null
}
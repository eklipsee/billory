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

export type Customer = {
  id: number
  name: string
  street: string
  zip: string
  city: string
  email?: string | null
  phone?: string | null
  notes?: string | null
  createdAt?: string
  updatedAt?: string
}

export type CustomerCreateRequest = {
  name: string
  street: string
  zip: string
  city: string
  email?: string
  phone?: string
  notes?: string
}

export type CustomerUpdateRequest = CustomerCreateRequest
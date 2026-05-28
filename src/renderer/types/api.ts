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

export type DocumentType = 'OFFER' | 'INVOICE'

export type DocumentStatus =
  | 'DRAFT'
  | 'OPEN'
  | 'PAID'
  | 'CANCELLED'



export type DocumentSummary = {
  id: number
  type: DocumentType
  status: DocumentStatus
  isHistorical?: boolean
  invoiceNumber?: string | null
  customerId: number
  customerName: string
  documentDate: string
  serviceDate?: string | null
  validUntil?: string | null
  grossTotal: number
  netTotal: number
  taxTotal: number
  pdfPath?: string | null
  notes?: string | null
  createdAt?: string
  updatedAt?: string
}

export type LineItemCreateRequest = {
  description: string
  netAmount: number
}

export type DocumentCreateRequest = {
  type: DocumentType
  customerId: number
  documentDate: string
  serviceDate?: string
  validUntil?: string
  notes?: string
  lineItems: LineItemCreateRequest[]
}

export type PdfPathResponse = {
  filePath: string
}

export type UpdateDocumentStatusRequest = {
  status: DocumentStatus
}

export type ConvertToInvoiceRequest = {
  offerId: number
}
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

export type HistoricalDocumentCreateRequest = {
  customerId: number
  invoiceNumber: string
  status: 'OPEN' | 'PAID'
  documentDate: string
  serviceDate: string
  notes?: string
  lineItems: LineItemCreateRequest[]
}

export type AttachPdfRequest = {
  sourceFilePath: string
}

export type ExternalInvoice = {
  id: number
  filePath: string
  year: number
  date: string
  description: string
  category?: string | null
  grossAmount: number
  netAmount: number
  taxAmount: number
  taxRate: number
  createdAt?: string
  updatedAt?: string
}

export type ExternalInvoiceCreateRequest = {
  sourceFilePath: string
  year: number
  date: string
  description: string
  category?: string
  grossAmount: number
}

export type ExternalInvoiceUpdateRequest = {
  date: string
  description: string
  category?: string
  grossAmount: number
}

export type ExternalInvoiceSummary = {
  totalGross: number
  totalNet: number
  totalTax: number
}

export type Settings = {
  id: number
  companyName: string
  ownerName: string
  street: string
  zip: string
  city: string
  phone?: string | null
  email?: string | null
  taxNumber: string
  iban?: string | null
  bankName?: string | null
  logoPath?: string | null
  archivePath: string
  backupPath?: string | null
  receiptsPath?: string | null
  reminderTemplate?: string | null
  invoicePrivacyNotice?: string | null
  offerWithdrawalNotice?: string | null
  createdAt?: string
  updatedAt?: string
}

export type SettingsUpdateRequest = Omit<
  Settings,
  'id' | 'createdAt' | 'updatedAt'
>

export type ChangePasswordRequest = {
  oldPassword: string
  newPassword: string
}
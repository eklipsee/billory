import { useEffect, useState } from 'react'

import { customerApi } from '../api/customerApi'

import type { Customer, DocumentStatus, DocumentType } from '../types/api'

import { documentApi } from '../api/documentApi'

function getTodayDate() {
  return new Date().toISOString().slice(0, 10)
}

export default function CreateDocumentPage() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [type, setType] = useState<DocumentType>('INVOICE')
  const [customerId, setCustomerId] = useState('')
  const [documentDate, setDocumentDate] = useState(getTodayDate())
  const [serviceDate, setServiceDate] = useState(getTodayDate())
  const [validUntil, setValidUntil] = useState('2026-06-05')
  const [notes, setNotes] = useState('')
  const [lineItems, setLineItems] = useState([
  {
    description: '',
    netAmount: '',
  },
])
  const [error, setError] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [isHistorical, setIsHistorical] = useState(false)
  const [invoiceNumber, setInvoiceNumber] = useState('')
  const [historicalStatus, setHistoricalStatus] =
    useState<DocumentStatus>('OPEN')

    useEffect(() => {
      async function loadCustomers() {
        try {
          const data = await customerApi.getAll()
          setCustomers(data)
        } catch {
          setError('Kunden konnten nicht geladen werden.')
        }
      }

      loadCustomers()
    }, [])


  function updateLineItem(
    index: number,
    field: 'description' | 'netAmount',
    value: string
    ) {
    setLineItems((currentItems) =>
        currentItems.map((item, currentIndex) => {
        if (currentIndex !== index) {
            return item
        }

        return {
            ...item,
            [field]: value,
        }
        })
    )
    }

    function addLineItem() {
    setLineItems((currentItems) => [
        ...currentItems,
        {
        description: '',
        netAmount: '',
        },
    ])
    }

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault()
        setError('')
        setIsSaving(true)

        try {
            if (isHistorical) {
            await documentApi.createHistorical({
                customerId: Number(customerId),
                invoiceNumber,
                status: historicalStatus === 'PAID' ? 'PAID' : 'OPEN',
                documentDate,
                serviceDate,
                notes,
                lineItems: lineItems.map((item) => ({
                description: item.description,
                netAmount: Number(item.netAmount),
                })),
            })
            } else {
            await documentApi.create({
                type,
                customerId: Number(customerId),
                documentDate,
                serviceDate: type === 'INVOICE' ? serviceDate : undefined,
                validUntil: type === 'OFFER' ? validUntil : undefined,
                notes,
                lineItems: lineItems.map((item) => ({
                description: item.description,
                netAmount: Number(item.netAmount),
                })),
            })
            }
            setCustomerId('')
            setDocumentDate(getTodayDate())
            setServiceDate(getTodayDate())
            setValidUntil('2026-06-05')
            setNotes('')
            setLineItems([
            {
                description: '',
                netAmount: '',
            },
            ])
            setIsHistorical(false)
            setInvoiceNumber('')
            setHistoricalStatus('OPEN')

            alert('Dokument wurde erstellt.')
        } catch (error) {
            setError(
            error instanceof Error
                ? error.message
                : 'Dokument konnte nicht erstellt werden.'
            )
        }finally {
            setIsSaving(false)
        }
    }

  return (
    <main>
      <h2>Dokument erstellen</h2>

      {error && <p>{error}</p>}

      <form onSubmit={handleSubmit}>
        <label>
            <input
                type="checkbox"
                checked={isHistorical}
                onChange={(event) => {
                setIsHistorical(event.target.checked)

                if (event.target.checked) {
                    setType('INVOICE')
                }
                }}
            />
            Historische Rechnung
        </label>
        <select
            value={type}
            disabled={isHistorical}
            onChange={(event) =>
                setType(event.target.value as DocumentType)
            }
        >
          <option value="INVOICE">Rechnung</option>
          <option value="OFFER">Angebot</option>
        </select>
        {isHistorical && (
        <>
            <input
            type="text"
            placeholder="Rechnungsnummer"
            value={invoiceNumber}
            onChange={(event) =>
                setInvoiceNumber(event.target.value)
            }
            />

            <select
            value={historicalStatus}
            onChange={(event) =>
                setHistoricalStatus(
                event.target.value as DocumentStatus
                )
            }
            >
            <option value="OPEN">Offen</option>
            <option value="PAID">Bezahlt</option>
            </select>
        </>
        )}

        <select
          value={customerId}
          onChange={(event) => setCustomerId(event.target.value)}
        >
          <option value="">Kunde auswählen</option>

          {customers.map((customer) => (
            <option key={customer.id} value={customer.id}>
              {customer.name}
            </option>
          ))}
        </select>

        <input
          type="date"
          value={documentDate}
          onChange={(event) => setDocumentDate(event.target.value)}
        />

        {type === 'INVOICE' && (
          <input
            type="date"
            value={serviceDate}
            onChange={(event) => setServiceDate(event.target.value)}
          />
        )}

        {type === 'OFFER' && (
          <input
            type="date"
            value={validUntil}
            onChange={(event) => setValidUntil(event.target.value)}
          />
        )}

        <input
          type="text"
          placeholder="Notizen"
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
        />

        <h3>Positionen</h3>

        {lineItems.map((item, index) => (
        <div key={index}>
            <input
            type="text"
            placeholder="Beschreibung"
            value={item.description}
            onChange={(event) =>
                updateLineItem(
                index,
                'description',
                event.target.value
                )
            }
            />

            <input
            type="number"
            placeholder="Netto"
            value={item.netAmount}
            onChange={(event) =>
                updateLineItem(
                index,
                'netAmount',
                event.target.value
                )
            }
            />
        </div>
        ))}

        <button
        type="button"
        onClick={addLineItem}
        >
            Position hinzufügen
        </button>
        <button type="submit" disabled={isSaving}>
            {isSaving ? 'Speichert...' : 'Dokument speichern'}
        </button>
      </form>
    </main>
  )
}
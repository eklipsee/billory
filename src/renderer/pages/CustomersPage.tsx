import { useEffect, useState } from 'react'

import { customerApi } from '../api/customerApi'

import type { Customer } from '../types/api'

type CustomerFormState = {
  name: string
  street: string
  zip: string
  city: string
  email: string
  phone: string
  notes: string
  distanceKm: string
}

const emptyForm: CustomerFormState = {
  name: '',
  street: '',
  zip: '',
  city: '',
  email: '',
  phone: '',
  notes: '',
  distanceKm: '',
}

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [search, setSearch] = useState('')
  const [form, setForm] = useState<CustomerFormState>(emptyForm)
  const [editingCustomerId, setEditingCustomerId] = useState<number | null>(null)
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  async function loadCustomers(searchValue = '') {
    setIsLoading(true)
    setError('')

    try {
      const data = await customerApi.getAll(searchValue)
      setCustomers(data)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Kunden konnten nicht geladen werden.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadCustomers(search)
  }, [search])

  function updateForm(field: keyof CustomerFormState, value: string) {
    setForm((currentForm) => ({
      ...currentForm,
      [field]: value,
    }))
  }

  function openCreateForm() {
    setEditingCustomerId(null)
    setForm(emptyForm)
    setIsFormOpen(true)
  }

  function closeForm() {
    setEditingCustomerId(null)
    setForm(emptyForm)
    setIsFormOpen(false)
  }

  async function handleSubmitCustomer(event: React.FormEvent) {
    event.preventDefault()
    setError('')

    try {
      if (editingCustomerId === null) {
        await customerApi.create({
          ...form,
          distanceKm:
            form.distanceKm.trim() === ''
              ? undefined
              : Number(form.distanceKm),
        })
      } else {
        await customerApi.update(editingCustomerId, {
          ...form,
          distanceKm:
            form.distanceKm.trim() === ''
              ? undefined
              : Number(form.distanceKm),
        })
      }

      closeForm()
      await loadCustomers(search)
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Kunde konnte nicht gespeichert werden.'
      )
    }
  }

  return (
    <main>
      <h2>Kunden</h2>

      <p className="page-subtitle">
        Verwalten Sie Ihre Kundenstammdaten.
      </p>

      {error && <p className="error">{error}</p>}

      <div className="customer-toolbar">
        <input
          type="text"
          placeholder="Kunde suchen..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />

        <button type="button" className="primary-button" onClick={openCreateForm}>
          + Neuer Kunde
        </button>
      </div>

      {isFormOpen && (
        <form className="customer-form-card" onSubmit={handleSubmitCustomer}>
          <div className="form-header">
            <h3>
              {editingCustomerId === null
                ? 'Neuen Kunden anlegen'
                : 'Kunden bearbeiten'}
            </h3>

            <button type="button" className="icon-button" onClick={closeForm}>
              ×
            </button>
          </div>

          <div className="customer-form-grid">
            <label>
              Name *
              <input
                type="text"
                placeholder="z. B. Max Mustermann"
                value={form.name}
                onChange={(event) => updateForm('name', event.target.value)}
              />
            </label>

            <label>
              Straße
              <input
                type="text"
                placeholder="z. B. Musterstraße 1"
                value={form.street}
                onChange={(event) => updateForm('street', event.target.value)}
              />
            </label>

            <label>
              PLZ
              <input
                type="text"
                placeholder="z. B. 12345"
                value={form.zip}
                onChange={(event) => updateForm('zip', event.target.value)}
              />
            </label>

            <label>
              Ort
              <input
                type="text"
                placeholder="z. B. Musterstadt"
                value={form.city}
                onChange={(event) => updateForm('city', event.target.value)}
              />
            </label>

            <label>
              E-Mail
              <input
                type="email"
                placeholder="z. B. max@example.com"
                value={form.email}
                onChange={(event) => updateForm('email', event.target.value)}
              />
            </label>

            <label>
              Telefon
              <input
                type="text"
                placeholder="z. B. 0123 456789"
                value={form.phone}
                onChange={(event) => updateForm('phone', event.target.value)}
              />
            </label>

            <label>
              Entfernung (km)
              <input
                type="number"
                step="0.1"
                min="0"
                placeholder="z. B. 18.5"
                value={form.distanceKm}
                onChange={(event) =>
                  updateForm('distanceKm', event.target.value)
                }
              />
            </label>

            <label className="notes-field">
              Notizen
              <input
                type="text"
                placeholder="Notizen optional"
                value={form.notes}
                onChange={(event) => updateForm('notes', event.target.value)}
              />
            </label>
          </div>

          <div className="form-actions">
            <button type="button" onClick={closeForm}>
              Abbrechen
            </button>

            <button type="submit" className="primary-button">
              Speichern
            </button>
          </div>
        </form>
      )}

      {isLoading && <p>Lade Kunden...</p>}

      {!isLoading && (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Adresse</th>
              <th>E-Mail</th>
              <th>Telefon</th>
              <th>Entfernung</th>
              <th>Aktionen</th>
            </tr>
          </thead>

          <tbody>
            {customers.length === 0 && (
              <tr>
                <td colSpan={6}>Keine Kunden gefunden.</td>
              </tr>
            )}

            {customers.map((customer) => (
              <tr key={customer.id}>
                <td>{customer.name}</td>
                <td>
                  {customer.street}, {customer.zip} {customer.city}
                </td>
                <td>{customer.email || '-'}</td>
                <td>{customer.phone || '-'}</td>
                <td>
                  {customer.distanceKm != null
                    ? `${customer.distanceKm.toLocaleString('de-DE')} km`
                    : '-'}
                </td>
                <td>
                  <div className="table-actions">
                    <button
                      type="button"
                      className="icon-action edit-action"
                      title="Bearbeiten"
                      onClick={() => {
                        setEditingCustomerId(customer.id)
                        setIsFormOpen(true)

                        setForm({
                          name: customer.name,
                          street: customer.street,
                          zip: customer.zip,
                          city: customer.city,
                          email: customer.email || '',
                          phone: customer.phone || '',
                          notes: customer.notes || '',
                          distanceKm:
                            customer.distanceKm != null
                              ? String(customer.distanceKm)
                              : '',
                        })
                      }}
                    >
                      ✎
                    </button>

                    <button
                      type="button"
                      className="icon-action delete-action"
                      title="Löschen"
                      onClick={async () => {
                        const confirmed = window.confirm(
                          `Kunde "${customer.name}" wirklich löschen?`
                        )

                        if (!confirmed) {
                          return
                        }

                        try {
                          await customerApi.delete(customer.id)
                          await loadCustomers(search)
                        } catch (error) {
                          setError(
                            error instanceof Error
                              ? error.message
                              : 'Kunde konnte nicht gelöscht werden.'
                          )
                        }
                      }}
                    >
                      🗑
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}
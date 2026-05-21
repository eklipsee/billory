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
}

const emptyForm: CustomerFormState = {
  name: '',
  street: '',
  zip: '',
  city: '',
  email: '',
  phone: '',
  notes: '',
}

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [search, setSearch] = useState('')
  const [form, setForm] = useState<CustomerFormState>(emptyForm)
  const [editingCustomerId, setEditingCustomerId] = useState<number | null>(null)
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

  async function handleSubmitCustomer(event: React.FormEvent) {
        event.preventDefault()
        setError('')

        try {
            if (editingCustomerId === null) {
            await customerApi.create(form)
            } else {
            await customerApi.update(editingCustomerId, form)
            }

            setForm(emptyForm)
            setEditingCustomerId(null)
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

      <form onSubmit={handleSubmitCustomer}>
        <h3>{editingCustomerId === null ? 'Neuen Kunden anlegen' : 'Kunden bearbeiten'}</h3>

        <input
          type="text"
          placeholder="Name"
          value={form.name}
          onChange={(event) => updateForm('name', event.target.value)}
        />

        <input
          type="text"
          placeholder="Straße"
          value={form.street}
          onChange={(event) => updateForm('street', event.target.value)}
        />

        <input
          type="text"
          placeholder="PLZ"
          value={form.zip}
          onChange={(event) => updateForm('zip', event.target.value)}
        />

        <input
          type="text"
          placeholder="Ort"
          value={form.city}
          onChange={(event) => updateForm('city', event.target.value)}
        />

        <input
          type="email"
          placeholder="E-Mail"
          value={form.email}
          onChange={(event) => updateForm('email', event.target.value)}
        />

        <input
          type="text"
          placeholder="Telefon"
          value={form.phone}
          onChange={(event) => updateForm('phone', event.target.value)}
        />

        <input
          type="text"
          placeholder="Notizen"
          value={form.notes}
          onChange={(event) => updateForm('notes', event.target.value)}
        />

        <button type="submit">
          {editingCustomerId === null
            ? 'Kunde speichern'
            : 'Änderungen speichern'}
        </button>

        {editingCustomerId !== null && (
          <button
            type="button"
            onClick={() => {
              setEditingCustomerId(null)
              setForm(emptyForm)
            }}
          >
            Abbrechen
          </button>
        )}
      </form>

      <hr />

      <input
        type="text"
        placeholder="Kunde suchen..."
        value={search}
        onChange={(event) => setSearch(event.target.value)}
      />

      {isLoading && <p>Lade Kunden...</p>}
      {error && <p>{error}</p>}

      {!isLoading && (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Adresse</th>
              <th>E-Mail</th>
              <th>Telefon</th>
              <th>Aktionen</th>
            </tr>
          </thead>

          <tbody>
            {customers.map((customer) => (
              <tr key={customer.id}>
                <td>{customer.name}</td>
                <td>
                  {customer.street}, {customer.zip} {customer.city}
                </td>
                <td>{customer.email || '-'}</td>
                <td>{customer.phone || '-'}</td>
                <td>
                  <button
                    type="button"
                    onClick={() => {
                      setEditingCustomerId(customer.id)

                      setForm({
                        name: customer.name,
                        street: customer.street,
                        zip: customer.zip,
                        city: customer.city,
                        email: customer.email || '',
                        phone: customer.phone || '',
                        notes: customer.notes || '',
                      })
                    }}
                  >
                    Bearbeiten
                  </button>
                  <button
                    type="button"
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
                    Löschen
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </main>
  )
}
import { useEffect, useState } from 'react'

import { settingsApi } from '../api/settingsApi'

import type { SettingsUpdateRequest } from '../types/api'

const emptySettings: SettingsUpdateRequest = {
  companyName: '',
  ownerName: '',
  street: '',
  zip: '',
  city: '',
  phone: '',
  email: '',
  taxNumber: '',
  iban: '',
  bankName: '',
  logoPath: '',
  archivePath: '',
  backupPath: '',
  receiptsPath: '',
  reminderTemplate: '',
  invoicePrivacyNotice: '',
  offerWithdrawalNotice: '',
}

export default function SettingsPage() {
  const [settings, setSettings] =
    useState<SettingsUpdateRequest>(emptySettings)

  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')

  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    async function loadSettings() {
      setIsLoading(true)
      setError('')

      try {
        const data = await settingsApi.get()

        setSettings({
          companyName: data.companyName,
          ownerName: data.ownerName,
          street: data.street,
          zip: data.zip,
          city: data.city,
          phone: data.phone || '',
          email: data.email || '',
          taxNumber: data.taxNumber,
          iban: data.iban || '',
          bankName: data.bankName || '',
          logoPath: data.logoPath || '',
          archivePath: data.archivePath,
          backupPath: data.backupPath || '',
          receiptsPath: data.receiptsPath || '',
          reminderTemplate: data.reminderTemplate || '',
          invoicePrivacyNotice: data.invoicePrivacyNotice || '',
          offerWithdrawalNotice: data.offerWithdrawalNotice || '',
        })
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : 'Einstellungen konnten nicht geladen werden.'
        )
      } finally {
        setIsLoading(false)
      }
    }

    loadSettings()
  }, [])

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError('')

    setIsSaving(true)

    try {
      await settingsApi.update(settings)
      alert('Einstellungen wurden gespeichert.')
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Einstellungen konnten nicht gespeichert werden.'
      )
    }finally {
      setIsSaving(false)
    }
  }

  async function handleChangePassword(event: React.FormEvent) {
    event.preventDefault()
    setError('')

    try {
      await settingsApi.changePassword({
        oldPassword,
        newPassword,
      })

      setOldPassword('')
      setNewPassword('')

      alert('Passwort wurde geändert.')
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Passwort konnte nicht geändert werden.'
      )
    }
  }

  return (
    <main>
      <h2>Einstellungen</h2>

      {isLoading && <p>Lade Einstellungen...</p>}
      {error && <p className="error">{error}</p>}

      <form onSubmit={handleSubmit}>
        <h3>Firmendaten</h3>

        <input
          type="text"
          placeholder="Firmenname"
          value={settings.companyName}
          onChange={(event) =>
            setSettings({ ...settings, companyName: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="Inhaber"
          value={settings.ownerName}
          onChange={(event) =>
            setSettings({ ...settings, ownerName: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="Straße"
          value={settings.street}
          onChange={(event) =>
            setSettings({ ...settings, street: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="PLZ"
          value={settings.zip}
          onChange={(event) =>
            setSettings({ ...settings, zip: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="Ort"
          value={settings.city}
          onChange={(event) =>
            setSettings({ ...settings, city: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="Telefon"
          value={settings.phone || ''}
          onChange={(event) =>
            setSettings({ ...settings, phone: event.target.value })
          }
        />

        <input
          type="email"
          placeholder="E-Mail"
          value={settings.email || ''}
          onChange={(event) =>
            setSettings({ ...settings, email: event.target.value })
          }
        />

        <h3>Steuer & Bank</h3>

        <input
          type="text"
          placeholder="Steuernummer"
          value={settings.taxNumber}
          onChange={(event) =>
            setSettings({ ...settings, taxNumber: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="IBAN"
          value={settings.iban || ''}
          onChange={(event) =>
            setSettings({ ...settings, iban: event.target.value })
          }
        />

        <input
          type="text"
          placeholder="Bank"
          value={settings.bankName || ''}
          onChange={(event) =>
            setSettings({ ...settings, bankName: event.target.value })
          }
        />

        <h3>Speicherorte</h3>

        <div className="storage-info">
          <p>
            Billory erstellt und verwaltet alle Ordner automatisch.
          </p>

          <strong>Dokumente/Billory</strong>

          <div className="folder-buttons">
            <button type="button" onClick={() => window.electronAPI.openBilloryFolder('Rechnungen')}>
              📁 Rechnungen öffnen
            </button>

            <button type="button" onClick={() => window.electronAPI.openBilloryFolder('Angebote')}>
              📁 Angebote öffnen
            </button>

            <button type="button" onClick={() => window.electronAPI.openBilloryFolder('Mahnungen')}>
              📁 Mahnungen öffnen
            </button>

            <button type="button" onClick={() => window.electronAPI.openBilloryFolder('Belege')}>
              📁 Belege öffnen
            </button>

            <button type="button" onClick={() => window.electronAPI.openBilloryFolder('Backups')}>
              📁 Backups öffnen
            </button>
          </div>
        </div>

        <h3>Texte</h3>

        <textarea
          placeholder="Mahnungsvorlage"
          value={settings.reminderTemplate || ''}
          onChange={(event) =>
            setSettings({ ...settings, reminderTemplate: event.target.value })
          }
        />

        <textarea
          placeholder="Datenschutzhinweis Rechnung"
          value={settings.invoicePrivacyNotice || ''}
          onChange={(event) =>
            setSettings({ ...settings, invoicePrivacyNotice: event.target.value })
          }
        />

        <textarea
          placeholder="Widerrufshinweis Angebot"
          value={settings.offerWithdrawalNotice || ''}
          onChange={(event) =>
            setSettings({ ...settings, offerWithdrawalNotice: event.target.value })
          }
        />

        <button type="submit" disabled={isSaving}>
          {isSaving ? 'Speichert...' : 'Einstellungen speichern'}
        </button>
      </form>

      <form onSubmit={handleChangePassword}>
        <h3>Passwort ändern</h3>

        <input
          type="password"
          placeholder="Altes Passwort"
          value={oldPassword}
          onChange={(event) => setOldPassword(event.target.value)}
        />

        <input
          type="password"
          placeholder="Neues Passwort"
          value={newPassword}
          onChange={(event) => setNewPassword(event.target.value)}
        />

        <button type="submit">
          Passwort ändern
        </button>
      </form>
    </main>
  )
}
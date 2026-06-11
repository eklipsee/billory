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
      <div className="page-header">
        <div>
          <h2>Einstellungen</h2>
          <p className="page-subtitle">
            Verwalten Sie Ihre Firmendaten, Speicherorte und Texte.
          </p>
        </div>
      </div>

      {isLoading && <p>Lade Einstellungen...</p>}
      {error && <p className="error">{error}</p>}

      <div className="settings-grid">
        <form onSubmit={handleSubmit} className="settings-card settings-company-card">
          <h3>Firmendaten</h3>

          <div className="settings-form-grid">
            <label>
              Firmenname
              <input
                type="text"
                value={settings.companyName}
                onChange={(event) =>
                  setSettings({ ...settings, companyName: event.target.value })
                }
              />
            </label>

            <label>
              Inhaber
              <input
                type="text"
                value={settings.ownerName}
                onChange={(event) =>
                  setSettings({ ...settings, ownerName: event.target.value })
                }
              />
            </label>

            <label>
              Straße
              <input
                type="text"
                value={settings.street}
                onChange={(event) =>
                  setSettings({ ...settings, street: event.target.value })
                }
              />
            </label>

            <label>
              PLZ
              <input
                type="text"
                value={settings.zip}
                onChange={(event) =>
                  setSettings({ ...settings, zip: event.target.value })
                }
              />
            </label>

            <label>
              Ort
              <input
                type="text"
                value={settings.city}
                onChange={(event) =>
                  setSettings({ ...settings, city: event.target.value })
                }
              />
            </label>

            <label>
              Telefon
              <input
                type="text"
                value={settings.phone || ''}
                onChange={(event) =>
                  setSettings({ ...settings, phone: event.target.value })
                }
              />
            </label>

            <label className="settings-full-width">
              E-Mail
              <input
                type="email"
                value={settings.email || ''}
                onChange={(event) =>
                  setSettings({ ...settings, email: event.target.value })
                }
              />
            </label>
          </div>
        </form>

        <form onSubmit={handleSubmit} className="settings-card settings-bank-card">
          <h3>Steuer & Bank</h3>

          <div className="settings-form-grid settings-form-grid-single">
            <label>
              Steuernummer
              <input
                type="text"
                value={settings.taxNumber}
                onChange={(event) =>
                  setSettings({ ...settings, taxNumber: event.target.value })
                }
              />
            </label>

            <label>
              IBAN
              <input
                type="text"
                value={settings.iban || ''}
                onChange={(event) =>
                  setSettings({ ...settings, iban: event.target.value })
                }
              />
            </label>

            <label>
              Bank
              <input
                type="text"
                value={settings.bankName || ''}
                onChange={(event) =>
                  setSettings({ ...settings, bankName: event.target.value })
                }
              />
            </label>
          </div>
        </form>

        <form onSubmit={handleSubmit} className="settings-card settings-storage-card">
          <h3>Speicherorte</h3>

          <p className="settings-card-description">
            Billory erstellt und verwaltet alle Ordner automatisch.
          </p>

          <div className="storage-path-box">
            Dokumente/Billory
          </div>

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
          </div>
        </form>

        <form onSubmit={handleSubmit} className="settings-card settings-text-card">
          <h3>Texte</h3>

          <div className="settings-form-grid settings-form-grid-single">
            <label>
              Mahnungsvorlage
              <textarea
                value={settings.reminderTemplate || ''}
                onChange={(event) =>
                  setSettings({ ...settings, reminderTemplate: event.target.value })
                }
              />
            </label>

            <label>
              Datenschutzhinweis Rechnung
              <textarea
                value={settings.invoicePrivacyNotice || ''}
                onChange={(event) =>
                  setSettings({ ...settings, invoicePrivacyNotice: event.target.value })
                }
              />
            </label>

            <label>
              Widerrufshinweis Angebot
              <textarea
                value={settings.offerWithdrawalNotice || ''}
                onChange={(event) =>
                  setSettings({ ...settings, offerWithdrawalNotice: event.target.value })
                }
              />
            </label>
          </div>

          <div className="form-actions">
            <button type="submit" className="primary-button" disabled={isSaving}>
              {isSaving ? 'Speichert...' : 'Einstellungen speichern'}
            </button>
          </div>
        </form>

        <form onSubmit={handleChangePassword} className="settings-card settings-password-card">
          <h3>Passwort ändern</h3>

          <div className="settings-form-grid settings-form-grid-single">
            <label>
              Altes Passwort
              <input
                type="password"
                value={oldPassword}
                onChange={(event) => setOldPassword(event.target.value)}
              />
            </label>

            <label>
              Neues Passwort
              <input
                type="password"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
              />
            </label>
          </div>

          <div className="form-actions">
            <button type="submit" className="primary-button">
              Passwort ändern
            </button>
          </div>
        </form>
      </div>
    </main>
  )
}
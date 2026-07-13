import { useState } from 'react'

import { settingsApi } from '../api/settingsApi'

import logo from '../assets/logo.png'
import forest from '../assets/forest.png'

type SetupPageProps = {
  onSetupComplete: () => void
}

export default function SetupPage({
  onSetupComplete,
}: SetupPageProps) {
  const [companyName, setCompanyName] = useState('')
  const [ownerName, setOwnerName] = useState('')
  const [street, setStreet] = useState('')
  const [zip, setZip] = useState('')
  const [city, setCity] = useState('')
  const [taxNumber, setTaxNumber] = useState('')

  const [password, setPassword] = useState('')
  const [passwordRepeat, setPasswordRepeat] = useState('')

  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError('')

    if (password !== passwordRepeat) {
      setError('Passwörter stimmen nicht überein.')
      return
    }

    setIsLoading(true)

    try {
      await settingsApi.create({
        companyName,
        ownerName,
        street,
        zip,
        city,
        phone: '',
        email: '',
        taxNumber,
        iban: '',
        bankName: '',
        logoPath: '',

        archivePath: 'C:/Temp/Archiv',
        backupPath: 'C:/Temp/Backup',
        receiptsPath: 'C:/Temp/Belege',

        reminderTemplate:
          'Bitte begleichen Sie den offenen Betrag.',
        invoicePrivacyNotice: 'Datenschutztext',
        offerWithdrawalNotice: 'Widerrufstext',
        password,
      })

      onSetupComplete()
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Einrichtung konnte nicht abgeschlossen werden.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="setup-screen">
      <div className="setup-shell">
        <section className="setup-brand-panel">
          <div className="setup-brand-logo-area">
            <img
              src={logo}
              alt="Baum Performance Stahl"
              className="setup-logo"
            />
          </div>

          <div className="setup-brand-copy">
            <h1>Willkommen bei Billory</h1>

            <p>
              Richten Sie Ihre Rechnungssoftware einmalig ein
              und legen Sie die wichtigsten Firmendaten fest.
            </p>

            <div className="setup-welcome-line" />
          </div>

          <div className="setup-brand-forest-area">
            <img
              src={forest}
              alt=""
              className="setup-forest"
            />
          </div>
        </section>

        <section className="setup-form-panel">
          <form
            onSubmit={handleSubmit}
            className="setup-form"
          >
            <div className="setup-title-row">
              <div className="setup-title-icon">⚙</div>

              <div>
                <h2>Ersteinrichtung</h2>

                <p>
                  Bitte erfassen Sie Ihre Firmendaten und
                  legen Sie ein Passwort fest.
                </p>
              </div>
            </div>

            <div className="setup-section-heading">
              <span>▥</span>
              <h3>Firmendaten</h3>
              <div />
            </div>

            <div className="setup-fields">
              <input
                type="text"
                placeholder="Firmenname"
                value={companyName}
                onChange={(event) =>
                  setCompanyName(event.target.value)
                }
              />

              <input
                type="text"
                placeholder="Inhaber / Ansprechpartner"
                value={ownerName}
                onChange={(event) =>
                  setOwnerName(event.target.value)
                }
              />

              <div className="setup-address-row">
                <input
                  type="text"
                  placeholder="Straße"
                  value={street}
                  onChange={(event) =>
                    setStreet(event.target.value)
                  }
                />

                <input
                  type="text"
                  placeholder="PLZ"
                  value={zip}
                  onChange={(event) =>
                    setZip(event.target.value)
                  }
                />
              </div>

              <input
                type="text"
                placeholder="Ort"
                value={city}
                onChange={(event) =>
                  setCity(event.target.value)
                }
              />

              <input
                type="text"
                placeholder="Steuernummer"
                value={taxNumber}
                onChange={(event) =>
                  setTaxNumber(event.target.value)
                }
              />
            </div>

            <div className="setup-section-heading">
              <span>⌑</span>
              <h3>Passwort festlegen</h3>
              <div />
            </div>

            <div className="setup-fields">
              <input
                type="password"
                placeholder="Passwort"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
              />

              <input
                type="password"
                placeholder="Passwort wiederholen"
                value={passwordRepeat}
                onChange={(event) =>
                  setPasswordRepeat(event.target.value)
                }
              />
            </div>

            <div className="setup-security-info">
              <span>✓</span>

              <p>
                Das Passwort wird sicher verschlüsselt gespeichert
                und bei jedem Start von Billory benötigt.
              </p>
            </div>

            {error && (
              <p className="error setup-error">
                {error}
              </p>
            )}

            <button
              type="submit"
              className="primary-button setup-submit-button"
              disabled={isLoading}
            >
              {isLoading
                ? 'Einrichtung wird abgeschlossen...'
                : '✓ Einrichtung abschließen'}
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}
import { useState } from 'react'

import { settingsApi } from '../api/settingsApi'

type SetupPageProps = {
  onSetupComplete: () => void
}

export default function SetupPage({ onSetupComplete }: SetupPageProps) {
  const [companyName, setCompanyName] = useState('')
  const [ownerName, setOwnerName] = useState('')
  const [street, setStreet] = useState('')
  const [zip, setZip] = useState('')
  const [city, setCity] = useState('')
  const [taxNumber, setTaxNumber] = useState('')
  const [archivePath, setArchivePath] = useState('C:/Temp/Archiv')
  const [backupPath, setBackupPath] = useState('C:/Temp/Backup')
  const [receiptsPath, setReceiptsPath] = useState('C:/Temp/Belege')
  const [password, setPassword] = useState('')
  const [passwordRepeat, setPasswordRepeat] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError('')

    if (password !== passwordRepeat) {
      setError('Passwörter stimmen nicht überein.')
      return
    }

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
        archivePath,
        backupPath,
        receiptsPath,
        reminderTemplate: 'Bitte begleichen Sie den offenen Betrag.',
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
    }
  }

  return (
    <main>
      <h2>Ersteinrichtung</h2>

      {error && <p className="error">{error}</p>}

      <form onSubmit={handleSubmit}>
        <input type="text" placeholder="Firmenname" value={companyName} onChange={(e) => setCompanyName(e.target.value)} />
        <input type="text" placeholder="Inhaber" value={ownerName} onChange={(e) => setOwnerName(e.target.value)} />
        <input type="text" placeholder="Straße" value={street} onChange={(e) => setStreet(e.target.value)} />
        <input type="text" placeholder="PLZ" value={zip} onChange={(e) => setZip(e.target.value)} />
        <input type="text" placeholder="Ort" value={city} onChange={(e) => setCity(e.target.value)} />
        <input type="text" placeholder="Steuernummer" value={taxNumber} onChange={(e) => setTaxNumber(e.target.value)} />

        <input type="text" placeholder="Archivpfad" value={archivePath} onChange={(e) => setArchivePath(e.target.value)} />
        <input type="text" placeholder="Backup-Pfad" value={backupPath} onChange={(e) => setBackupPath(e.target.value)} />
        <input type="text" placeholder="Belegpfad" value={receiptsPath} onChange={(e) => setReceiptsPath(e.target.value)} />

        <input type="password" placeholder="Passwort" value={password} onChange={(e) => setPassword(e.target.value)} />
        <input type="password" placeholder="Passwort wiederholen" value={passwordRepeat} onChange={(e) => setPasswordRepeat(e.target.value)} />

        <button type="submit">
          Einrichtung abschließen
        </button>
      </form>
    </main>
  )
}
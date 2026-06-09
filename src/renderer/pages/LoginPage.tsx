import { useState } from 'react'
import { authApi } from '../api/authApi'

type LoginPageProps = {
  onLoginSuccess: () => void
}

export default function LoginPage({
  onLoginSuccess,
}: LoginPageProps) {
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()

    setError('')
    setIsLoading(true)

    try {
      const result = await authApi.login({ password })

      if (!result.success) {
        setError('Passwort ist falsch.')
        return
      }

      onLoginSuccess()
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : 'Login fehlgeschlagen.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main>
      <h1>Billory</h1>

      <p>Bitte Passwort eingeben.</p>

      <form onSubmit={handleSubmit}>
        <input
          type="password"
          placeholder="Passwort"
          value={password}
          onChange={(event) =>
            setPassword(event.target.value)
          }
        />

        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Prüfe...' : 'Einloggen'}
        </button>
      </form>

      {error && <p className="error">{error}</p>}
    </main>
  )
}
import { useState } from 'react'
import { authApi } from '../api/authApi'
import logo from '../assets/logo.png'
import forest from '../assets/forest.png'

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

      await window.electronAPI.maximizeApp()

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
    <div className="auth-screen">
      <div className="auth-card">
        <img
          src={logo}
          alt="Baum Performance Stahl"
          className="auth-logo"
        />

        <form onSubmit={handleSubmit} className="auth-login-content">
          <h2>Willkommen zurück</h2>

          <label>
            Passwort

            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              autoFocus
            />
          </label>

          <button type="submit" className="primary-button" disabled={isLoading}>
            {isLoading ? 'Prüfe...' : 'Anmelden'}
          </button>

          {error && <p className="auth-error">{error}</p>}
        </form>

        <img
          src={forest}
          alt=""
          className="auth-forest"
        />
      </div>
    </div>
  )
}
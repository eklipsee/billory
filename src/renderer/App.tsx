import { useEffect, useState } from 'react'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  useEffect(() => {
    const storedLogin = localStorage.getItem('isLoggedIn')

    if (storedLogin === 'true') {
      setIsLoggedIn(true)
    }
  }, [])

  function handleLoginSuccess() {
    localStorage.setItem('isLoggedIn', 'true')
    setIsLoggedIn(true)
  }

  function handleLogout() {
    localStorage.removeItem('isLoggedIn')
    setIsLoggedIn(false)
  }

  if (!isLoggedIn) {
    return (
      <LoginPage onLoginSuccess={handleLoginSuccess} />
    )
  }

  return <DashboardPage onLogout={handleLogout} />
}
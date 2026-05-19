import { useEffect, useState } from 'react'

import AppLayout from './layouts/AppLayout'

import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'
import DocumentsPage from './pages/DocumentsPage'
import ExternalInvoicesPage from './pages/ExternalInvoicesPage'
import ExportPage from './pages/ExportPage'
import SettingsPage from './pages/SettingsPage'

import type { AppPage } from './types/navigation'

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  const [activePage, setActivePage] =
    useState<AppPage>('dashboard')

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

  function renderPage() {
    switch (activePage) {
      case 'dashboard':
        return <DashboardPage />

      case 'customers':
        return <CustomersPage />

      case 'documents':
        return <DocumentsPage />

      case 'externalInvoices':
        return <ExternalInvoicesPage />

      case 'export':
        return <ExportPage />

      case 'settings':
        return <SettingsPage />

      default:
        return <DashboardPage />
    }
  }

  if (!isLoggedIn) {
    return (
      <LoginPage onLoginSuccess={handleLoginSuccess} />
    )
  }

  return (
    <AppLayout
      activePage={activePage}
      onNavigate={setActivePage}
      onLogout={handleLogout}
    >
      {renderPage()}
    </AppLayout>
  )
}
import { useEffect, useState } from 'react'

import AppLayout from './layouts/AppLayout'

import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CustomersPage from './pages/CustomersPage'
import DocumentsPage from './pages/DocumentsPage'
import ExternalInvoicesPage from './pages/ExternalInvoicesPage'
import ExportPage from './pages/ExportPage'
import SettingsPage from './pages/SettingsPage'
import CreateDocumentPage from './pages/CreateDocumentPage'

import { settingsApi } from './api/settingsApi'
import SetupPage from './pages/SetupPage'

import type { AppPage } from './types/navigation'

export default function App() {

  const [isCheckingSetup, setIsCheckingSetup] = useState(true)
  const [needsSetup, setNeedsSetup] = useState(false)
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  const [activePage, setActivePage] =
    useState<AppPage>('dashboard')

  useEffect(() => {
    async function checkSetup() {
      try {
        await settingsApi.get()

        const storedLogin = localStorage.getItem('isLoggedIn')

        if (storedLogin === 'true') {
          setIsLoggedIn(true)
        }

        setNeedsSetup(false)
      } catch {
        setNeedsSetup(true)
        localStorage.removeItem('isLoggedIn')
        setIsLoggedIn(false)
      } finally {
        setIsCheckingSetup(false)
      }
    }

    checkSetup()
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

      case 'createDocument':
        return <CreateDocumentPage />

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

  if (isCheckingSetup) {
    return <p>Prüfe Einrichtung...</p>
  }

  if (needsSetup) {
    return (
      <SetupPage
        onSetupComplete={() => {
          localStorage.removeItem('isLoggedIn')
          setIsLoggedIn(false)
          setNeedsSetup(false)
        }}
      />
    )
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
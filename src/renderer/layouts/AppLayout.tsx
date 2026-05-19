import type { AppPage } from '../types/navigation'

type AppLayoutProps = {
  activePage: AppPage
  onNavigate: (page: AppPage) => void
  onLogout: () => void
  children: React.ReactNode
}

const navigationItems: { page: AppPage; label: string }[] = [
  { page: 'dashboard', label: 'Dashboard' },
  { page: 'customers', label: 'Kunden' },
  { page: 'documents', label: 'Dokumente' },
  { page: 'externalInvoices', label: 'Belege' },
  { page: 'export', label: 'Export' },
  { page: 'settings', label: 'Einstellungen' },
]

export default function AppLayout({
  activePage,
  onNavigate,
  onLogout,
  children,
}: AppLayoutProps) {
  return (
    <div className="app-layout">
      <aside className="sidebar">
        <h1>Billory</h1>

        <nav>
          {navigationItems.map((item) => (
            <button
                key={item.page}
                type="button"
                className={
                    activePage === item.page ? 'active' : ''
                }
                onClick={() => onNavigate(item.page)}
            >
              {item.label}
            </button>
          ))}
        </nav>

        <button type="button" onClick={onLogout}>
          Abmelden
        </button>
      </aside>

      <section className="content">{children}</section>
    </div>
  )
}
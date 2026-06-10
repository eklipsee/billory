import logo from '../assets/tree.png'
import type { AppPage } from '../types/navigation'

type AppLayoutProps = {
  activePage: AppPage
  onNavigate: (page: AppPage) => void
  onLogout: () => void
  children: React.ReactNode
}

const navigationItems: { page: AppPage; label: string; icon: string }[] = [
  { page: 'dashboard', label: 'Dashboard', icon: '▦' },
  { page: 'customers', label: 'Kunden', icon: '👤' },
  { page: 'documents', label: 'Dokumente', icon: '📄' },
  { page: 'createDocument', label: 'Dokument erstellen', icon: '＋' },
  { page: 'externalInvoices', label: 'Belege', icon: '🧾' },
  { page: 'export', label: 'Export', icon: '⇩' },
  { page: 'settings', label: 'Einstellungen', icon: '⚙' },
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
        <div className="sidebar-brand">
          <img src={logo} alt="Baum Performance Stahl" />
        </div>

        <nav className="sidebar-nav">
          {navigationItems.map((item) => (
            <button
              key={item.page}
              type="button"
              className={activePage === item.page ? 'active' : ''}
              onClick={() => onNavigate(item.page)}
            >
              <span className="nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <button type="button" onClick={onLogout}>
            Abmelden
          </button>
        </div>
      </aside>

      <section className="content">{children}</section>
    </div>
  )
}
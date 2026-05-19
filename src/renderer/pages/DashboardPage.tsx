type DashboardPageProps = {
  onLogout: () => void
}

export default function DashboardPage({
  onLogout,
}: DashboardPageProps) {
  return (
    <main>
      <h1>Billory Dashboard</h1>

      <p>Frontend erfolgreich verbunden.</p>

      <button type="button" onClick={onLogout}>
        Abmelden
      </button>
    </main>
  )
}
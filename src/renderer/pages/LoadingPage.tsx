import logo from '../assets/logo.png'
import forest from '../assets/forest.png'

export default function LoadingPage() {
  return (
    <div className="auth-screen">
      <div className="auth-card">

        <img
          src={logo}
          alt="Baum Performance Stahl"
          className="auth-logo"
        />

        <div className="auth-loading-content">
          <div className="auth-progress">
            <div className="auth-progress-bar" />
          </div>

          <h2>Billory wird gestartet...</h2>

          <p>
            Bitte einen Moment warten.
          </p>
        </div>

        <img
          src={forest}
          alt=""
          className="auth-forest"
        />
      </div>
    </div>
  )
}
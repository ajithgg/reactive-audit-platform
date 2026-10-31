import { Link, Outlet } from 'react-router-dom'
import { useSession } from '../session'

export function Layout() {
  const { session, setSession } = useSession()

  return (
    <div className="app-shell">
      <div className="atmosphere" aria-hidden="true" />
      <header className="topbar">
        <div className="brand-block">
          <Link to="/" className="brand">
            Tenant Console
          </Link>
          <p className="brand-sub">User CRUD via user-tenant · audits flow to Elasticsearch</p>
        </div>
        <div className="session-controls">
          <label>
            Tenant
            <input
              value={session.tenantId}
              onChange={(e) => setSession({ ...session, tenantId: e.target.value })}
              spellCheck={false}
            />
          </label>
          <label>
            Actor
            <input
              value={session.actorId}
              onChange={(e) => setSession({ ...session, actorId: e.target.value })}
              spellCheck={false}
            />
          </label>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  )
}

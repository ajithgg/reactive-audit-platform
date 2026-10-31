import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/client'
import { listUsers } from '../api/users'
import { EyeIcon, PlusIcon } from '../components/Icons'
import { StatusBadge } from '../components/StatusBadge'
import { useSession } from '../session'
import type { User } from '../types'

export function UsersPage() {
  const { session } = useSession()
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    listUsers(session)
      .then((data) => {
        if (!cancelled) setUsers(data)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : 'Failed to load users')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [session])

  return (
    <section className="panel fade-in">
      <div className="panel-header">
        <div>
          <h1>Users</h1>
          <p className="lede">All active and inactive users for the current tenant.</p>
        </div>
        <Link to="/users/new" className="btn btn-primary">
          <PlusIcon />
          Create user
        </Link>
      </div>

      {loading && <p className="muted">Loading users…</p>}
      {error && <p className="error-banner">{error}</p>}

      {!loading && !error && (
        <div className="table-wrap">
          <table className="users-table">
            <thead>
              <tr>
                <th>Display name</th>
                <th>External ID</th>
                <th>Email</th>
                <th>Status</th>
                <th>Updated</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="empty-cell">
                    No users yet. Create the first one.
                  </td>
                </tr>
              ) : (
                users.map((user, index) => (
                  <tr key={user.id} style={{ animationDelay: `${index * 40}ms` }} className="row-enter">
                    <td className="strong">{user.displayName}</td>
                    <td className="mono">{user.externalId}</td>
                    <td>{user.email}</td>
                    <td>
                      <StatusBadge status={user.status} />
                    </td>
                    <td className="mono muted">{formatDate(user.updatedAt)}</td>
                    <td className="actions">
                      <Link
                        to={`/users/${user.id}`}
                        className="icon-btn"
                        title="View user"
                        aria-label={`View ${user.displayName}`}
                      >
                        <EyeIcon />
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

function formatDate(value: string) {
  try {
    return new Intl.DateTimeFormat(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value))
  } catch {
    return value
  }
}

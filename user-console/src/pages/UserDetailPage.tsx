import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { deleteUser, getUser, updateUser } from '../api/users'
import { ArrowLeftIcon } from '../components/Icons'
import { StatusBadge } from '../components/StatusBadge'
import { useSession } from '../session'
import type { User } from '../types'

export function UserDetailPage() {
  const { id = '' } = useParams()
  const { session } = useSession()
  const navigate = useNavigate()

  const [user, setUser] = useState<User | null>(null)
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [status, setStatus] = useState('ACTIVE')
  const [department, setDepartment] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    setNotice(null)

    getUser(session, id)
      .then((data) => {
        if (cancelled) return
        setUser(data)
        setEmail(data.email)
        setDisplayName(data.displayName)
        setStatus(data.status)
        setDepartment(String(data.attributes?.department ?? ''))
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : 'Failed to load user')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [id, session])

  async function onSave(event: FormEvent) {
    event.preventDefault()
    if (!user) return
    setSaving(true)
    setError(null)
    setNotice(null)
    try {
      const updated = await updateUser(session, user.id, {
        email: email.trim(),
        displayName: displayName.trim(),
        status,
        attributes: {
          ...user.attributes,
          department: department.trim() || undefined,
        },
      })
      setUser(updated)
      setNotice('Saved. An UPDATE audit event was published.')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to save user')
    } finally {
      setSaving(false)
    }
  }

  async function onDelete() {
    if (!user) return
    if (!window.confirm(`Delete ${user.displayName}? This records a DELETE audit event.`)) return
    setDeleting(true)
    setError(null)
    try {
      await deleteUser(session, user.id)
      navigate('/')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to delete user')
      setDeleting(false)
    }
  }

  if (loading) {
    return (
      <section className="panel fade-in">
        <p className="muted">Loading user…</p>
      </section>
    )
  }

  if (!user) {
    return (
      <section className="panel fade-in">
        <Link to="/" className="back-link">
          <ArrowLeftIcon />
          Back to users
        </Link>
        <p className="error-banner">{error ?? 'User not found'}</p>
      </section>
    )
  }

  return (
    <section className="panel fade-in narrow">
      <Link to="/" className="back-link">
        <ArrowLeftIcon />
        Back to users
      </Link>

      <div className="panel-header">
        <div>
          <div className="title-row">
            <h1>{user.displayName}</h1>
            <StatusBadge status={user.status} />
          </div>
          <p className="lede mono">{user.id}</p>
        </div>
        <button type="button" className="btn btn-danger" onClick={onDelete} disabled={deleting}>
          {deleting ? 'Deleting…' : 'Delete'}
        </button>
      </div>

      <dl className="meta-grid">
        <div>
          <dt>External ID</dt>
          <dd className="mono">{user.externalId}</dd>
        </div>
        <div>
          <dt>Tenant</dt>
          <dd className="mono">{user.tenantId}</dd>
        </div>
        <div>
          <dt>Version</dt>
          <dd className="mono">{user.version}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd className="mono">{formatDate(user.createdAt)}</dd>
        </div>
      </dl>

      <form className="form-grid" onSubmit={onSave}>
        <label>
          Display name
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required />
        </label>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Status
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </label>
        <label>
          Department
          <input value={department} onChange={(e) => setDepartment(e.target.value)} />
        </label>

        {error && <p className="error-banner span-2">{error}</p>}
        {notice && <p className="success-banner span-2">{notice}</p>}

        <div className="form-actions span-2">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save changes'}
          </button>
        </div>
      </form>
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

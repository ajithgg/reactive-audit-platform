import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { createUser } from '../api/users'
import { ArrowLeftIcon } from '../components/Icons'
import { useSession } from '../session'

export function CreateUserPage() {
  const { session } = useSession()
  const navigate = useNavigate()
  const [externalId, setExternalId] = useState('')
  const [email, setEmail] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [status, setStatus] = useState('ACTIVE')
  const [department, setDepartment] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const user = await createUser(session, {
        externalId: externalId.trim(),
        email: email.trim(),
        displayName: displayName.trim(),
        status,
        attributes: department.trim() ? { department: department.trim() } : {},
      })
      navigate(`/users/${user.id}`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to create user')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="panel fade-in narrow">
      <Link to="/" className="back-link">
        <ArrowLeftIcon />
        Back to users
      </Link>
      <div className="panel-header">
        <div>
          <h1>Create user</h1>
          <p className="lede">Creates via user-tenant and records a CREATE audit event.</p>
        </div>
      </div>

      <form className="form-grid" onSubmit={onSubmit}>
        <label>
          External ID
          <input value={externalId} onChange={(e) => setExternalId(e.target.value)} required spellCheck={false} />
        </label>
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
        <label className="span-2">
          Department
          <input
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
            placeholder="optional attribute"
          />
        </label>

        {error && <p className="error-banner span-2">{error}</p>}

        <div className="form-actions span-2">
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Creating…' : 'Create user'}
          </button>
        </div>
      </form>
    </section>
  )
}

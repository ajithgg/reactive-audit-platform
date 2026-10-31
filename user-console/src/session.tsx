import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { SessionHeaders } from './types'

const STORAGE_KEY = 'user-console.session'

type SessionContextValue = {
  session: SessionHeaders
  setSession: (next: SessionHeaders) => void
}

const SessionContext = createContext<SessionContextValue | null>(null)

function readStored(): SessionHeaders {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw) as SessionHeaders
      if (parsed.tenantId && parsed.actorId) return parsed
    }
  } catch {
    // ignore
  }
  return { tenantId: 'tenant-a', actorId: 'ajith' }
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<SessionHeaders>(readStored)

  const setSession = (next: SessionHeaders) => {
    setSessionState(next)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  }

  const value = useMemo(() => ({ session, setSession }), [session])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

export function useSession() {
  const ctx = useContext(SessionContext)
  if (!ctx) throw new Error('useSession must be used within SessionProvider')
  return ctx
}

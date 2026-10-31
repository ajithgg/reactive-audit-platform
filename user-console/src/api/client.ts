import type { SessionHeaders } from '../types'

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
    this.name = 'ApiError'
  }
}

export async function apiRequest<T>(
  path: string,
  session: SessionHeaders,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  headers.set('X-Tenant-Id', session.tenantId)
  headers.set('X-Actor-Id', session.actorId)
  headers.set('X-Actor-Type', 'USER')

  const response = await fetch(path, { ...init, headers })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const body = text ? safeJson(text) : null

  if (!response.ok) {
    let message = text || `Request failed (${response.status})`
    if (body && typeof body === 'object') {
      if ('error' in body && body.error != null) message = String(body.error)
      else if ('message' in body && body.message != null) message = String(body.message)
    }
    throw new ApiError(response.status, message)
  }

  return body as T
}

function safeJson(text: string): unknown {
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

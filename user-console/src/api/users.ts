import type { CreateUserPayload, SessionHeaders, UpdateUserPayload, User } from '../types'
import { apiRequest } from './client'

const BASE = '/api/v1/users'

export function listUsers(session: SessionHeaders) {
  return apiRequest<User[]>(BASE, session)
}

export function getUser(session: SessionHeaders, id: string) {
  return apiRequest<User>(`${BASE}/${id}`, session)
}

export function createUser(session: SessionHeaders, payload: CreateUserPayload) {
  return apiRequest<User>(BASE, session, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateUser(session: SessionHeaders, id: string, payload: UpdateUserPayload) {
  return apiRequest<User>(`${BASE}/${id}`, session, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function deleteUser(session: SessionHeaders, id: string) {
  return apiRequest<void>(`${BASE}/${id}`, session, {
    method: 'DELETE',
  })
}

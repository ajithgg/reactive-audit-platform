export type User = {
  id: string
  tenantId: string
  externalId: string
  email: string
  displayName: string
  status: string
  attributes: Record<string, unknown>
  version: number
  createdAt: string
  updatedAt: string
}

export type CreateUserPayload = {
  externalId: string
  email: string
  displayName: string
  status: string
  attributes: Record<string, unknown>
}

export type UpdateUserPayload = {
  email?: string
  displayName?: string
  status?: string
  attributes?: Record<string, unknown>
}

export type SessionHeaders = {
  tenantId: string
  actorId: string
}

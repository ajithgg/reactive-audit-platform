type Props = { status: string }

export function StatusBadge({ status }: Props) {
  const normalized = status.toUpperCase()
  const tone = normalized === 'ACTIVE' ? 'active' : normalized === 'INACTIVE' ? 'inactive' : 'other'
  return <span className={`status-badge status-badge--${tone}`}>{normalized}</span>
}

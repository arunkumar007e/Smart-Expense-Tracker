import { formatINR } from '../utils/format'

interface StatCardProps {
  label: string
  value: number
  tone?: 'neutral' | 'income' | 'expense'
}

export default function StatCard({ label, value, tone = 'neutral' }: StatCardProps) {
  return (
    <div className="stat-card">
      <span className="stat-label">{label}</span>
      <span className={`stat-value ${tone}`}>{formatINR(value)}</span>
    </div>
  )
}
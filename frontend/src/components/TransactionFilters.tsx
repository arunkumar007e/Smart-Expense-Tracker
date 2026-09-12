import type { Category, TransactionFilters, TransactionType } from '../types'

interface TransactionFiltersProps {
  categories: Category[]
  filters: TransactionFilters
  onChange: (filters: TransactionFilters) => void
  onReset: () => void
}

const TYPE_OPTIONS: Array<{ value: '' | TransactionType; label: string }> = [
  { value: '', label: 'All types' },
  { value: 'INCOME', label: 'Income' },
  { value: 'EXPENSE', label: 'Expense' }
]

const FILTER_MONTHS = (() => {
  const months = []
  const now = new Date()
  for (let i = 0; i < 12; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return months
})()

export default function TransactionFilters({
  categories,
  filters,
  onChange,
  onReset
}: TransactionFiltersProps) {
  return (
    <div className="filter-bar">
      <select
        aria-label="Type filter"
        value={filters.type}
        onChange={(event) => onChange({ ...filters, type: event.target.value as TransactionFilters['type'] })}
      >
        {TYPE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>

      <select
        aria-label="Category filter"
        value={filters.categoryId}
        onChange={(event) =>
          onChange({ ...filters, categoryId: event.target.value ? Number(event.target.value) : '' })
        }
      >
        <option value="">All categories</option>
        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.name}
          </option>
        ))}
      </select>

      <select
        aria-label="Month filter"
        value={filters.month}
        onChange={(event) => onChange({ ...filters, month: event.target.value })}
      >
        <option value="">All months</option>
        {FILTER_MONTHS.map((month) => (
          <option key={month} value={month}>
            {month}
          </option>
        ))}
      </select>

      <input
        type="search"
        placeholder="Search description…"
        value={filters.search}
        onChange={(event) => onChange({ ...filters, search: event.target.value })}
      />

      {(filters.type !== '' || filters.categoryId !== '' || filters.month !== '' || filters.search !== '') && (
        <button className="btn btn-ghost btn-small" onClick={onReset}>
          Clear
        </button>
      )}
    </div>
  )
}
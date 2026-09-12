import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import type { Category, Transaction, TransactionInput, TransactionType } from '../types'
import { currentMonth } from '../utils/format'

interface TransactionFormProps {
  categories: Category[]
  editing: Transaction | null
  defaultType: TransactionType
  submitting: boolean
  error: string | null
  onSubmit: (input: TransactionInput) => void
  onCancel: () => void
}

const COLORS: Record<TransactionType, string> = {
  INCOME: '#10B981',
  EXPENSE: '#EF4444'
}

export default function TransactionForm({
  categories,
  editing,
  defaultType,
  submitting,
  error,
  onSubmit,
  onCancel
}: TransactionFormProps) {
  const [type, setType] = useState<TransactionType>(editing?.type ?? defaultType)
  const [categoryId, setCategoryId] = useState<string>(editing ? String(editing.category.id) : '')
  const [amount, setAmount] = useState(editing ? String(editing.amount) : '')
  const [description, setDescription] = useState(editing?.description ?? '')
  const [date, setDate] = useState(editing?.transactionDate ?? currentMonth() + '-01')
  const [localError, setLocalError] = useState<string | null>(null)

  const filteredCategories = categories.filter((category) => category.type === type)

  useEffect(() => {
    const first = filteredCategories[0]
    if (editing && editing.category.type === type) {
      setCategoryId(String(editing.category.id))
    } else if (!categoryId && first) {
      setCategoryId(String(first.id))
    } else if (filteredCategories.length > 0 && !filteredCategories.some((c) => c.id === Number(categoryId))) {
      setCategoryId(String(first.id))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [type, categories])

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const amountNumber = Number(amount)
    if (!amount || Number.isNaN(amountNumber) || amountNumber <= 0) {
      setLocalError('Enter a valid amount greater than zero.')
      return
    }
    if (!date) {
      setLocalError('Choose a date.')
      return
    }
    if (!categoryId) {
      setLocalError(`Select a ${type.toLowerCase()} category.`)
      return
    }
    setLocalError(null)
    onSubmit({
      type,
      categoryId: Number(categoryId),
      amount: amountNumber,
      transactionDate: date,
      description: description.trim()
    })
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="segmented">
        {(['EXPENSE', 'INCOME'] as TransactionType[]).map((option) => (
          <button
            key={option}
            type="button"
            className={`segment${type === option ? ' segment-active' : ''}`}
            style={type === option ? { background: COLORS[option], borderColor: COLORS[option] } : undefined}
            onClick={() => {
              setType(option)
              setLocalError(null)
            }}
          >
            {option.charAt(0) + option.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      <div className="row">
        <div className="field">
          <label htmlFor="tx-amount">Amount (₹)</label>
          <input
            id="tx-amount"
            type="number"
            step="0.01"
            min="0.01"
            placeholder="0.00"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
            autoFocus
          />
        </div>
        <div className="field">
          <label htmlFor="tx-category">Category</label>
          <select
            id="tx-category"
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
          >
            <option value="">Select…</option>
            {filteredCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="row">
        <div className="field">
          <label htmlFor="tx-date">Date</label>
          <input
            id="tx-date"
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="tx-description">Description (optional)</label>
          <input
            id="tx-description"
            type="text"
            placeholder="e.g. Groceries at BigBasket"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </div>
      </div>

      {(localError || error) && <div className="alert alert-error">{localError ?? error}</div>}

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : editing ? 'Save changes' : `Add ${type.toLowerCase()}`}
        </button>
      </div>
    </form>
  )
}
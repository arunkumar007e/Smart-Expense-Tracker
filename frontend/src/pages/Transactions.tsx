import { useCallback, useEffect, useRef, useState } from 'react'
import { categoryApi } from '../api/categoryApi'
import { transactionApi } from '../api/transactionApi'
import ConfirmDialog from '../components/ConfirmDialog'
import Modal from '../components/Modal'
import TransactionFilters from '../components/TransactionFilters'
import TransactionForm from '../components/TransactionForm'
import TransactionTable from '../components/TransactionTable'
import { currentMonth } from '../utils/format'
import type {
  Category,
  Transaction,
  TransactionFilters as Filters,
  TransactionInput,
  TransactionType
} from '../types'

const emptyFilters: Filters = { type: '', categoryId: '', month: currentMonth(), search: '' }

export default function Transactions() {
  const [categories, setCategories] = useState<Category[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [filters, setFilters] = useState<Filters>(emptyFilters)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Transaction | null>(null)
  const [formDefaultType, setFormDefaultType] = useState<TransactionType>('EXPENSE')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [deleting, setDeleting] = useState<Transaction | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const searchTimer = useRef<number | null>(null)

  useEffect(() => {
    categoryApi
      .list()
      .then(setCategories)
      .catch(() => setCategories([]))
  }, [])

  const loadTransactions = useCallback((nextFilters: Filters) => {
    setLoading(true)
    transactionApi
      .list(nextFilters)
      .then(setTransactions)
      .catch((err: unknown) =>
        setError(err instanceof Error ? err.message : 'Failed to load transactions')
      )
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    loadTransactions(filters)
  }, [filters.month, filters.categoryId, filters.type, loadTransactions])

  useEffect(() => {
    if (searchTimer.current !== null) {
      window.clearTimeout(searchTimer.current)
    }
    searchTimer.current = window.setTimeout(() => {
      loadTransactions(filters)
    }, 350)
    return () => {
      if (searchTimer.current !== null) window.clearTimeout(searchTimer.current)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.search])

  const openCreate = (type: TransactionType = 'EXPENSE') => {
    setEditing(null)
    setFormDefaultType(type)
    setFormError(null)
    setFormOpen(true)
  }

  const openEdit = (transaction: Transaction) => {
    setEditing(transaction)
    setFormDefaultType(transaction.type)
    setFormError(null)
    setFormOpen(true)
  }

  const handleSubmit = async (input: TransactionInput) => {
    setSubmitting(true)
    setFormError(null)
    try {
      if (editing) {
        await transactionApi.update(editing.id, input)
      } else {
        await transactionApi.create(input)
      }
      setFormOpen(false)
      loadTransactions(filters)
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save transaction')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    try {
      await transactionApi.delete(deleting.id)
      setDeleting(null)
      loadTransactions(filters)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete transaction')
      setDeleting(null)
    } finally {
      setDeleteBusy(false)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Transactions</h2>
        <div className="btn-group">
          <button className="btn btn-income" onClick={() => openCreate('INCOME')}>
            + Income
          </button>
          <button className="btn btn-primary" onClick={() => openCreate('EXPENSE')}>
            + Expense
          </button>
        </div>
      </div>

      <TransactionFilters
        categories={categories}
        filters={filters}
        onChange={setFilters}
        onReset={() => setFilters(emptyFilters)}
      />

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <p className="muted">Loading…</p>
      ) : (
        <TransactionTable
          transactions={transactions}
          onEdit={openEdit}
          onDelete={setDeleting}
        />
      )}

      {formOpen && (
        <Modal
          title={editing ? 'Edit transaction' : `Add ${formDefaultType.toLowerCase()}`}
          onClose={() => setFormOpen(false)}
        >
          <TransactionForm
            categories={categories}
            editing={editing}
            defaultType={formDefaultType}
            submitting={submitting}
            error={formError}
            onSubmit={handleSubmit}
            onCancel={() => setFormOpen(false)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete transaction"
          message={`Delete “${deleting.description || deleting.category.name}” of ${deleting.amount}?`}
          busy={deleteBusy}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </div>
  )
}
import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { categoryApi } from '../api/categoryApi'
import ConfirmDialog from '../components/ConfirmDialog'
import Modal from '../components/Modal'
import type { Category, CategoryInput, TransactionType } from '../types'

const COLOR_PALETTE = ['#EF4444', '#3B82F6', '#8B5CF6', '#F59E0B', '#EC4899', '#14B8A6', '#22C55E', '#6366F1', '#10B981', '#F97316']

export default function Categories() {
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<Category | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [formType, setFormType] = useState<TransactionType>('EXPENSE')
  const [formName, setFormName] = useState('')
  const [formColor, setFormColor] = useState(COLOR_PALETTE[0])
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [deleting, setDeleting] = useState<Category | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)

  const load = () => {
    setLoading(true)
    categoryApi
      .list()
      .then(setCategories)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load categories'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const openCreate = (type: TransactionType) => {
    setEditing(null)
    setFormType(type)
    setFormName('')
    setFormColor(COLOR_PALETTE[Math.floor(Math.random() * COLOR_PALETTE.length)])
    setFormError(null)
    setModalOpen(true)
  }

  const openEdit = (category: Category) => {
    setEditing(category)
    setFormType(category.type)
    setFormName(category.name)
    setFormColor(category.color)
    setFormError(null)
    setModalOpen(true)
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const name = formName.trim()
    if (name.length < 2) {
      setFormError('Category name must be at least 2 characters.')
      return
    }
    const input: CategoryInput = { name, type: formType, color: formColor }
    setSubmitting(true)
    setFormError(null)
    try {
      if (editing) {
        await categoryApi.update(editing.id, input)
      } else {
        await categoryApi.create(input)
      }
      setModalOpen(false)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save category')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    try {
      await categoryApi.delete(deleting.id)
      setDeleting(null)
      load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete category')
      setDeleting(null)
    } finally {
      setDeleteBusy(false)
    }
  }

  const expenseCategories = categories.filter((category) => category.type === 'EXPENSE')
  const incomeCategories = categories.filter((category) => category.type === 'INCOME')

  const renderSection = (title: string, items: Category[]) => (
    <div className="card">
      <h3>{title}</h3>
      {items.length === 0 ? (
        <p className="empty-state">No {title.toLowerCase()} categories.</p>
      ) : (
        <ul className="category-list">
          {items.map((category) => (
            <li key={category.id} className="category-item">
              <span className="category-dot" style={{ background: category.color }} />
              <span className="category-name">{category.name}</span>
              <div className="row-actions">
                <button className="btn btn-ghost btn-small" onClick={() => openEdit(category)}>
                  Edit
                </button>
                <button
                  className="btn btn-ghost-danger btn-small"
                  onClick={() => setDeleting(category)}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )

  return (
    <div className="page">
      <div className="page-header">
        <h2>Categories</h2>
        <div className="btn-group">
          <button className="btn btn-income" onClick={() => openCreate('INCOME')}>
            + Income category
          </button>
          <button className="btn btn-primary" onClick={() => openCreate('EXPENSE')}>
            + Expense category
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <p className="muted">Loading…</p>
      ) : (
        <div className="category-grid">
          {renderSection('Expense', expenseCategories)}
          {renderSection('Income', incomeCategories)}
        </div>
      )}

      {modalOpen && (
        <Modal title={editing ? 'Edit category' : 'Add category'} onClose={() => setModalOpen(false)}>
          <form className="form" onSubmit={handleSubmit}>
            <div className="segmented">
              {(['EXPENSE', 'INCOME'] as TransactionType[]).map((option) => (
                <button
                  key={option}
                  type="button"
                  className={`segment${formType === option ? ' segment-active' : ''}`}
                  onClick={() => setFormType(option)}
                >
                  {option.charAt(0) + option.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
            <div className="field">
              <label htmlFor="cat-name">Name</label>
              <input
                id="cat-name"
                type="text"
                value={formName}
                onChange={(event) => setFormName(event.target.value)}
                placeholder="e.g. Groceries"
                autoFocus
              />
            </div>
            <div className="field">
              <label>Color</label>
              <div className="color-palette">
                {COLOR_PALETTE.map((color) => (
                  <button
                    key={color}
                    type="button"
                    aria-label={`Pick color ${color}`}
                    className={`swatch${formColor === color ? ' swatch-selected' : ''}`}
                    style={{ background: color }}
                    onClick={() => setFormColor(color)}
                  />
                ))}
              </div>
            </div>
            {formError && <div className="alert alert-error">{formError}</div>}
            <div className="modal-actions">
              <button type="button" className="btn" onClick={() => setModalOpen(false)} disabled={submitting}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete category"
          message={`Delete “${deleting.name}”? Categories that are used by transactions cannot be deleted.`}
          busy={deleteBusy}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </div>
  )
}
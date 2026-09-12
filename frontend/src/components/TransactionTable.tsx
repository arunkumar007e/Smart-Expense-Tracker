import { formatDate, formatINR } from '../utils/format'
import type { Transaction } from '../types'

interface TransactionTableProps {
  transactions: Transaction[]
  onEdit: (transaction: Transaction) => void
  onDelete: (transaction: Transaction) => void
}

export default function TransactionTable({
  transactions,
  onEdit,
  onDelete
}: TransactionTableProps) {
  if (transactions.length === 0) {
    return <p className="empty-state">No transactions found.</p>
  }

  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Category</th>
            <th>Description</th>
            <th>Amount</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.id}>
              <td>{formatDate(transaction.transactionDate)}</td>
              <td>
                <span className={`badge badge-${transaction.type.toLowerCase()}`}>
                  {transaction.type}
                </span>
              </td>
              <td>
                <span
                  className="category-dot"
                  style={{ background: transaction.category.color }}
                />
                {transaction.category.name}
              </td>
              <td className="muted">{transaction.description || '—'}</td>
              <td className={`amount amount-${transaction.type.toLowerCase()}`}>
                {transaction.type === 'INCOME' ? '+' : '−'}
                {formatINR(transaction.amount)}
              </td>
              <td>
                <div className="row-actions">
                  <button className="btn btn-ghost btn-small" onClick={() => onEdit(transaction)}>
                    Edit
                  </button>
                  <button
                    className="btn btn-ghost-danger btn-small"
                    onClick={() => onDelete(transaction)}
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
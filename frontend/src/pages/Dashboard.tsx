import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { dashboardApi } from '../api/dashboardApi'
import StatCard from '../components/StatCard'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  CartesianGrid
} from 'recharts'
import { formatINR, formatMonth, formatDate } from '../utils/format'
import type { DashboardSummary } from '../types'

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    dashboardApi
      .summary()
      .then(setSummary)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load dashboard'))
  }, [])

  if (error) {
    return <div className="page"><div className="alert alert-error">{error}</div></div>
  }

  if (!summary) {
    return <div className="page">Loading dashboard…</div>
  }

  const series = summary.monthlySeries.map((point) => ({
    ...point,
    monthLabel: formatMonth(point.month)
  }))

  const pieData = summary.categoryBreakdown
    .filter((entry) => entry.total > 0)
    .map((entry) => ({ name: entry.category, value: entry.total, color: entry.color }))
  const hasPieData = pieData.length > 0
  const hasSeries = series.some((point) => point.income > 0 || point.expense > 0)

  return (
    <div className="page">
      <div className="page-header">
        <h2>Dashboard</h2>
        <Link to="/transactions" className="btn btn-primary">
          + Add transaction
        </Link>
      </div>

      <div className="stats">
        <StatCard label="Total income" value={summary.totalIncome} tone="income" />
        <StatCard label="Total expense" value={summary.totalExpense} tone="expense" />
        <StatCard label="Balance" value={summary.balance} />
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <h3>Last 6 months</h3>
          {hasSeries ? (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={series}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="monthLabel" />
                <YAxis tickFormatter={(value: number) => `₹${value / 1000}k`} width={60} />
                <Tooltip
                  formatter={(value: number) => formatINR(value)}
                  labelStyle={{ color: '#1e293b' }}
                />
                <Legend />
                <Bar dataKey="income" name="Income" fill="#10B981" />
                <Bar dataKey="expense" name="Expense" fill="#EF4444" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="empty-state">No data yet.</p>
          )}
        </div>

        <div className="card">
          <h3>Spending by category (all time)</h3>
          {hasPieData ? (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={55}
                  outerRadius={90}
                  paddingAngle={2}
                >
                  {pieData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value: number) => formatINR(value)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="empty-state">No expenses recorded yet.</p>
          )}
        </div>
      </div>

      <div className="card">
        <div className="page-header">
          <h3>Recent transactions</h3>
          <Link to="/transactions" className="btn btn-ghost btn-small">
            View all
          </Link>
        </div>
        {summary.recentTransactions.length === 0 ? (
          <p className="empty-state">
            No transactions yet. Add your first one from the Transactions page.
          </p>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th>Amount</th>
                </tr>
              </thead>
              <tbody>
                {summary.recentTransactions.map((transaction) => (
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
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
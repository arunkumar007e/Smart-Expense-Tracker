import { useCallback, useEffect, useState } from 'react'
import { reportApi } from '../api/reportApi'
import StatCard from '../components/StatCard'
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { currentMonth, formatINR, formatMonth } from '../utils/format'
import type { CategoryTotal, MonthlyReport, TransactionType } from '../types'

type Tab = 'category' | 'monthly'

const MONTHS = (() => {
  const months = []
  const now = new Date()
  for (let i = 0; i < 24; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return months
})()

export default function Reports() {
  const [tab, setTab] = useState<Tab>('category')
  const [month, setMonth] = useState(currentMonth())
  const [type, setType] = useState<TransactionType | ''>('')
  const [categoryData, setCategoryData] = useState<CategoryTotal[]>([])
  const [monthlyReport, setMonthlyReport] = useState<MonthlyReport | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const loadCategory = useCallback(() => {
    setLoading(true)
    reportApi
      .category(month, type || undefined)
      .then(setCategoryData)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load report'))
      .finally(() => setLoading(false))
  }, [month, type])

  const loadMonthly = useCallback(() => {
    setLoading(true)
    reportApi
      .monthly(month)
      .then(setMonthlyReport)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load report'))
      .finally(() => setLoading(false))
  }, [month])

  useEffect(() => {
    setError(null)
    if (tab === 'category') {
      loadCategory()
    } else {
      loadMonthly()
    }
  }, [tab, loadCategory, loadMonthly])

  const total = categoryData.reduce((sum, entry) => sum + entry.total, 0)
  const pieData = categoryData
    .filter((entry) => entry.total > 0)
    .map((entry) => ({ name: entry.category, value: entry.total, color: entry.color }))

  return (
    <div className="page">
      <div className="page-header">
        <h2>Reports</h2>
        <div className="filter-bar filter-bar-right">
          <select aria-label="Month" value={month} onChange={(event) => setMonth(event.target.value)}>
            {MONTHS.map((option) => (
              <option key={option} value={option}>
                {formatMonth(option)}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="tabs">
        {(
          [
            { value: 'category', label: 'Category breakdown' },
            { value: 'monthly', label: 'Monthly summary' }
          ] as Array<{ value: Tab; label: string }>
        ).map((tabOption) => (
          <button
            key={tabOption.value}
            className={`tab${tab === tabOption.value ? ' tab-active' : ''}`}
            onClick={() => setTab(tabOption.value)}
          >
            {tabOption.label}
          </button>
        ))}
      </div>

      {tab === 'category' && (
        <>
          <div className="filter-bar">
            <select
              aria-label="Type"
              value={type}
              onChange={(event) => setType(event.target.value as TransactionType | '')}
            >
              <option value="">Expenses & income</option>
              <option value="EXPENSE">Expenses</option>
              <option value="INCOME">Income</option>
            </select>
          </div>

          {error && <div className="alert alert-error">{error}</div>}
          {loading ? (
            <p className="muted">Loading…</p>
          ) : categoryData.length === 0 ? (
            <p className="empty-state">No data for the selected period.</p>
          ) : (
            <div className="report-grid">
              <div className="card">
                <h3>Total: {formatINR(total)}</h3>
                <div className="table-wrap">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Category</th>
                        <th>Transactions</th>
                        <th>Amount</th>
                        <th>Share</th>
                      </tr>
                    </thead>
                    <tbody>
                      {categoryData.map((entry) => (
                        <tr key={entry.category}>
                          <td>
                            <span className="category-dot" style={{ background: entry.color }} />
                            {entry.category}
                          </td>
                          <td>{entry.count}</td>
                          <td className="amount">{formatINR(entry.total)}</td>
                          <td>{total > 0 ? ((entry.total / total) * 100).toFixed(1) : '0.0'}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
              <div className="card">
                <h3>Distribution</h3>
                {pieData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={pieData}
                        dataKey="value"
                        nameKey="name"
                        innerRadius={60}
                        outerRadius={100}
                        paddingAngle={2}
                        label={({ name }) => name}
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
                  <p className="empty-state">Nothing to chart.</p>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {tab === 'monthly' && (
        <>
          {error && <div className="alert alert-error">{error}</div>}
          {loading ? (
            <p className="muted">Loading…</p>
          ) : monthlyReport ? (
            <>
              <div className="stats">
                <StatCard label="Income" value={monthlyReport.totalIncome} tone="income" />
                <StatCard label="Expense" value={monthlyReport.totalExpense} tone="expense" />
                <StatCard label="Balance" value={monthlyReport.balance} />
              </div>
              <div className="card">
                <h3>{formatMonth(monthlyReport.month)} — {monthlyReport.transactionCount} transactions</h3>
                {monthlyReport.categoryBreakdown.length === 0 ? (
                  <p className="empty-state">No transactions in this month.</p>
                ) : (
                  <div className="table-wrap">
                    <table className="table">
                      <thead>
                        <tr>
                          <th>Category</th>
                          <th>Transactions</th>
                          <th>Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {monthlyReport.categoryBreakdown.map((entry) => (
                          <tr key={entry.category}>
                            <td>
                              <span className="category-dot" style={{ background: entry.color }} />
                              {entry.category}
                            </td>
                            <td>{entry.count}</td>
                            <td className="amount">{formatINR(entry.total)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          ) : null}
        </>
      )}
    </div>
  )
}
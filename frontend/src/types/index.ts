export type TransactionType = 'INCOME' | 'EXPENSE'

export interface User {
  id: number
  name: string
  email: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: User
}

export interface Category {
  id: number
  name: string
  type: TransactionType
  color: string
}

export interface CategoryInput {
  name: string
  type: TransactionType
  color: string
}

export interface Transaction {
  id: number
  type: TransactionType
  amount: number
  description: string
  transactionDate: string
  createdAt: string
  category: Category
}

export interface TransactionInput {
  type: TransactionType
  amount: number
  categoryId: number
  transactionDate: string
  description: string
}

export interface CategoryTotal {
  category: string
  color: string
  count: number
  total: number
}

export interface MonthlyPoint {
  month: string
  income: number
  expense: number
}

export interface DashboardSummary {
  totalIncome: number
  totalExpense: number
  balance: number
  monthlyIncome: number
  monthlyExpense: number
  totalTransactions: number
  recentTransactions: Transaction[]
  categoryBreakdown: CategoryTotal[]
  monthlySeries: MonthlyPoint[]
}

export interface MonthlyReport {
  month: string
  totalIncome: number
  totalExpense: number
  balance: number
  transactionCount: number
  categoryBreakdown: CategoryTotal[]
}

export interface TransactionFilters {
  type: TransactionType | ''
  categoryId: number | ''
  month: string
  search: string
}
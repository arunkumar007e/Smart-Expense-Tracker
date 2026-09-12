import { api } from './client'
import type { Transaction, TransactionFilters, TransactionInput } from '../types'

export const transactionApi = {
  list: (filters: Partial<TransactionFilters> = {}) => {
    const params = new URLSearchParams()
    if (filters.type) params.set('type', filters.type)
    if (filters.categoryId) params.set('categoryId', String(filters.categoryId))
    if (filters.month) params.set('month', filters.month)
    if (filters.search) params.set('search', filters.search)
    const query = params.toString()
    return api.get<Transaction[]>(`/transactions${query ? `?${query}` : ''}`)
  },
  get: (id: number) => api.get<Transaction>(`/transactions/${id}`),
  create: (data: TransactionInput) => api.post<Transaction>('/transactions', data),
  update: (id: number, data: TransactionInput) => api.put<Transaction>(`/transactions/${id}`, data),
  delete: (id: number) => api.delete<void>(`/transactions/${id}`)
}
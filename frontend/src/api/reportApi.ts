import { api } from './client'
import type { MonthlyReport, CategoryTotal } from '../types'

export const reportApi = {
  monthly: (month: string) => api.get<MonthlyReport>(`/reports/monthly?month=${month}`),
  category: (month?: string, type?: string) => {
    const params = new URLSearchParams()
    if (month) params.set('month', month)
    if (type) params.set('type', type)
    const query = params.toString()
    return api.get<CategoryTotal[]>(`/reports/category${query ? `?${query}` : ''}`)
  }
}
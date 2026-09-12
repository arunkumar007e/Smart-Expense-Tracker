import { api } from './client'
import type { Category, CategoryInput } from '../types'

export const categoryApi = {
  list: () => api.get<Category[]>('/categories'),
  create: (data: CategoryInput) => api.post<Category>('/categories', data),
  update: (id: number, data: CategoryInput) => api.put<Category>(`/categories/${id}`, data),
  delete: (id: number) => api.delete<void>(`/categories/${id}`)
}
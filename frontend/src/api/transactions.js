import api from './client'

export const transactionApi = {
  history: (params) => api.get('/transactions', { params }).then((r) => r.data),
  details: (id) => api.get(`/transactions/${id}`).then((r) => r.data),
  receiptUrl: (id) => `/api/transactions/${id}/receipt`,
}

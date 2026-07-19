import api from './client'

export const walletApi = {
  get: () => api.get('/wallet').then((r) => r.data),
  addMoney: (data) => api.post('/wallet/add', data).then((r) => r.data),
  toBank: (data) => api.post('/wallet/to-bank', data).then((r) => r.data),
}

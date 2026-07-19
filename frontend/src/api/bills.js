import api from './client'

export const billApi = {
  billers: (category) => api.get('/bills/billers', { params: { category } }).then((r) => r.data),
  pay: (data) => api.post('/bills/pay', data).then((r) => r.data),
  history: () => api.get('/bills/history').then((r) => r.data),
}

import api from './client'

export const scheduledPaymentApi = {
  create: (data) => api.post('/scheduled-payments', data).then((r) => r.data),
  list: () => api.get('/scheduled-payments').then((r) => r.data),
  cancel: (id) => api.delete(`/scheduled-payments/${id}`).then((r) => r.data),
}

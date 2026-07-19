import api from './client'

export const paymentApi = {
  send: (data) => api.post('/payment/send', data).then((r) => r.data),
  selfTransfer: (data) => api.post('/payment/self-transfer', data).then((r) => r.data),
  request: (data) => api.post('/payment/request', data).then((r) => r.data),
  respond: (id, data) => api.post(`/payment/requests/${id}/respond`, data).then((r) => r.data),
  incoming: () => api.get('/payment/requests/incoming').then((r) => r.data),
  outgoing: () => api.get('/payment/requests/outgoing').then((r) => r.data),
}

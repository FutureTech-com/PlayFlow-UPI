import api from './client'

export const upiApi = {
  create: (data) => api.post('/upi/create', data).then((r) => r.data),
  list: () => api.get('/upi').then((r) => r.data),
  details: (vpa) => api.get('/upi/details', { params: { vpa } }).then((r) => r.data),
}

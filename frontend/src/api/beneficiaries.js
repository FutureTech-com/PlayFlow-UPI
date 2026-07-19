import api from './client'

export const beneficiaryApi = {
  add: (data) => api.post('/beneficiaries', data).then((r) => r.data),
  list: () => api.get('/beneficiaries').then((r) => r.data),
  toggleFavourite: (id) => api.patch(`/beneficiaries/${id}/favourite`).then((r) => r.data),
  remove: (id) => api.delete(`/beneficiaries/${id}`).then((r) => r.data),
}

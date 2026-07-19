import api from './client'

export const deviceApi = {
  list: () => api.get('/devices').then((r) => r.data),
  trust: (id) => api.patch(`/devices/${id}/trust`).then((r) => r.data),
  remove: (id) => api.delete(`/devices/${id}`).then((r) => r.data),
}

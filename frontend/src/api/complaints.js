import api from './client'

export const complaintApi = {
  create: (data) => api.post('/complaints', data).then((r) => r.data),
  mine: () => api.get('/complaints').then((r) => r.data),
}

import api from './client'

export const analyticsApi = {
  spending: () => api.get('/analytics/spending').then((r) => r.data),
}

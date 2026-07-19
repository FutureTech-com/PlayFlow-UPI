import api from './client'

export const rewardApi = {
  summary: () => api.get('/rewards').then((r) => r.data),
}

import api from './client'

export const aiApi = {
  insights: () => api.get('/ai/insights').then((r) => r.data),
  budgetSuggestions: () => api.get('/ai/budget-suggestions').then((r) => r.data),
  chat: (message) => api.post('/ai/chat', { message }).then((r) => r.data),
  myFraudAlerts: () => api.get('/ai/fraud-alerts').then((r) => r.data),
}

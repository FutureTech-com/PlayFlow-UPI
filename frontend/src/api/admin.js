import api from './client'

export const adminApi = {
  dashboard: () => api.get('/admin/dashboard').then((r) => r.data),
  users: () => api.get('/admin/users').then((r) => r.data),
  setUserEnabled: (id, enabled) =>
    api.patch(`/admin/users/${id}/enabled`, null, { params: { enabled } }).then((r) => r.data),
  updateKyc: (id, status) => api.patch(`/admin/users/${id}/kyc`, { status }).then((r) => r.data),
  transactions: () => api.get('/admin/transactions').then((r) => r.data),
  complaints: () => api.get('/admin/complaints').then((r) => r.data),
  resolveComplaint: (id, status, resolutionNote) =>
    api.post(`/admin/complaints/${id}/resolve`, { status, resolutionNote }).then((r) => r.data),
  cashbackOverview: () => api.get('/admin/cashback-overview').then((r) => r.data),
  fraudAlerts: () => api.get('/admin/fraud-alerts').then((r) => r.data),
}

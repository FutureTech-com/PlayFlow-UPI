import api from './client'

export const splitBillApi = {
  create: (data) => api.post('/split-bills', data).then((r) => r.data),
  organized: () => api.get('/split-bills/organized').then((r) => r.data),
  myShares: () => api.get('/split-bills/my-shares').then((r) => r.data),
  settle: (shareId, transactionPin) =>
    api.post(`/split-bills/shares/${shareId}/settle`, { transactionPin }).then((r) => r.data),
}

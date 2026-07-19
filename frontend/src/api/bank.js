import api from './client'

export const bankApi = {
  link: (data) => api.post('/bank/link', data).then((r) => r.data),
  fetchByMobile: (mobileNumber) => api.post('/bank/fetch-by-mobile', { mobileNumber }).then((r) => r.data),
  linkByMobile: (mobileNumber, accountIndex) =>
    api.post('/bank/link-by-mobile', { mobileNumber, accountIndex }).then((r) => r.data),
  list: () => api.get('/bank/accounts').then((r) => r.data),
  setPrimary: (id) => api.patch(`/bank/accounts/${id}/primary`).then((r) => r.data),
}

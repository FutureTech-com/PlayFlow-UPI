import api from './client'

export const userApi = {
  getProfile: () => api.get('/user/profile').then((r) => r.data),
  updateProfile: (data) => api.put('/user/profile', data).then((r) => r.data),
  setTransactionPin: (data) => api.post('/user/transaction-pin', data).then((r) => r.data),
  sendPhoneOtp: () => api.post('/user/phone/send-otp').then((r) => r.data),
  verifyPhoneOtp: (otp) => api.post('/user/phone/verify-otp', { otp }).then((r) => r.data),
  forgotPin: () => api.post('/user/transaction-pin/forgot').then((r) => r.data),
  resetPin: (otp, newPin) => api.post('/user/transaction-pin/reset', { otp, newPin }).then((r) => r.data),
}

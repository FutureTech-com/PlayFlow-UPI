import api from './client'

export const authApi = {
  register: (data) => api.post('/auth/register', data).then((r) => r.data),
  login: (data) => api.post('/auth/login', data).then((r) => r.data),
  sendMobileLoginOtp: (phone) => api.post('/auth/login/mobile/send-otp', { phone }).then((r) => r.data),
  loginWithMobileOtp: (phone, otp) => api.post('/auth/login/mobile/verify-otp', { phone, otp }).then((r) => r.data),
  changePassword: (data) => api.post('/auth/change-password', data).then((r) => r.data),
  forgotPassword: (data) => api.post('/auth/forgot-password', data).then((r) => r.data),
  resetPassword: (data) => api.post('/auth/reset-password', data).then((r) => r.data),
}

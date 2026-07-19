import api from './client'

export const notificationApi = {
  list: () => api.get('/notifications').then((r) => r.data),
  unreadCount: () => api.get('/notifications/unread-count').then((r) => r.data),
  markAllRead: () => api.post('/notifications/mark-all-read').then((r) => r.data),
}

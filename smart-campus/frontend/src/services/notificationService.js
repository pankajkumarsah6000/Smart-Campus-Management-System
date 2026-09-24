import api from './api';

export const notificationService = {
  list: () => api.get('/notifications/me').then((r) => r.data.data),
  unreadCount: () => api.get('/notifications/me/unread-count').then((r) => r.data.data.count),
  markRead: (id) => api.put(`/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => api.put('/notifications/me/read-all').then((r) => r.data),
};

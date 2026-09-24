import api from './api';

export const leaveService = {
  apply: (payload) => api.post('/leave', payload).then((r) => r.data.data),
  myRequests: () => api.get('/leave/me').then((r) => r.data.data),
  pending: () => api.get('/leave/pending').then((r) => r.data.data),
  all: () => api.get('/leave/all').then((r) => r.data.data),
  review: (id, payload) => api.put(`/leave/${id}/review`, payload).then((r) => r.data.data),
};

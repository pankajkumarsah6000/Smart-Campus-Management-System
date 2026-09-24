import api from './api';

export const complaintService = {
  raise: (payload) => api.post('/complaints', payload).then((r) => r.data.data),
  my: () => api.get('/complaints/me').then((r) => r.data.data),
  all: () => api.get('/complaints').then((r) => r.data.data),
  resolve: (id, payload) => api.put(`/complaints/${id}/resolve`, payload).then((r) => r.data.data),
};
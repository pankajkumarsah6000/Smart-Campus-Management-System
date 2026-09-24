import api from './api';

export const assignmentService = {
  create: (payload) => api.post('/assignments', payload).then((r) => r.data.data),
  update: (id, payload) => api.put(`/assignments/${id}`, payload).then((r) => r.data.data),
  remove: (id) => api.delete(`/assignments/${id}`).then((r) => r.data.data),
  my: () => api.get('/assignments/my').then((r) => r.data.data),
  detail: (id) => api.get(`/assignments/${id}`).then((r) => r.data.data),
  submit: (id, payload) => api.post(`/assignments/${id}/submit`, payload).then((r) => r.data.data),
  submissions: (id) => api.get(`/assignments/${id}/submissions`).then((r) => r.data.data),
  mySubmissions: () => api.get('/assignments/submissions/me').then((r) => r.data.data),
  grade: (submissionId, payload) =>
    api.put(`/assignments/submissions/${submissionId}/grade`, payload).then((r) => r.data.data),
};
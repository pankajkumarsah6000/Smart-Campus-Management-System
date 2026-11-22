import api from './api';

export const examinationService = {
  create: (payload) => api.post('/exams', payload).then((r) => r.data.data),
  update: (id, payload) => api.put(`/exams/${id}`, payload).then((r) => r.data.data),
  remove: (id) => api.delete(`/exams/${id}`).then((r) => r.data.data),
  list: () => api.get('/exams').then((r) => r.data.data),
  myExams: () => api.get('/exams/my').then((r) => r.data.data),
  nextExam: () => api.get('/exams/next').then((r) => r.data.data),
  myResults: () => api.get('/exams/results/me').then((r) => r.data.data),
  resultsForExam: (id) => api.get(`/exams/${id}/results`).then((r) => r.data.data),
  recordResult: (examId, payload) => api.post(`/exams/${examId}/results`, payload).then((r) => r.data.data),
  updateResult: (resultId, payload) => api.put(`/exams/results/${resultId}`, payload).then((r) => r.data.data),
};
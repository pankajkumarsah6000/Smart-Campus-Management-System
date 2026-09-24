import api from './api';

export const feeService = {
  create: (payload) => api.post('/fees', payload).then((r) => r.data.data),
  all: () => api.get('/fees').then((r) => r.data.data),
  myFees: () => api.get('/fees/me').then((r) => r.data.data),
  forStudent: (studentId) => api.get(`/fees/student/${studentId}`).then((r) => r.data.data),
  pay: (feeId, payload) => api.post(`/fees/${feeId}/pay`, payload).then((r) => r.data.data),
  remove: (feeId) => api.delete(`/fees/${feeId}`).then((r) => r.data.data),
};
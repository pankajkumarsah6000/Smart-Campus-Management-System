import api from './api';

export const timetableService = {
  create: (payload) => api.post('/timetable', payload).then((r) => r.data.data),
  update: (id, payload) => api.put(`/timetable/${id}`, payload).then((r) => r.data.data),
  remove: (id) => api.delete(`/timetable/${id}`).then((r) => r.data.data),
  bySection: (section, semester) =>
    api.get('/timetable', { params: { section, semester } }).then((r) => r.data.data),
  my: () => api.get('/timetable/my').then((r) => r.data.data),
};
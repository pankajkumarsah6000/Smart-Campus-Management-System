import api from './api';

export const parentService = {
  me: () => api.get('/parents/me').then((r) => r.data.data),
  children: () => api.get('/parents/me/children').then((r) => r.data.data),
  childDashboard: (studentId) => api.get(`/parents/children/${studentId}/dashboard`).then((r) => r.data.data),
  childAttendance: (studentId) => api.get(`/parents/children/${studentId}/attendance`).then((r) => r.data.data),
  childResults: (studentId) => api.get(`/parents/children/${studentId}/results`).then((r) => r.data.data),
  childFees: (studentId) => api.get(`/parents/children/${studentId}/fees`).then((r) => r.data.data),
  childTimetable: (studentId) => api.get(`/parents/children/${studentId}/timetable`).then((r) => r.data.data),
};
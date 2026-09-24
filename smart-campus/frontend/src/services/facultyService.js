import api from './api';

export const facultyService = {
  me: () => api.get('/faculty/me').then((r) => r.data.data),
  dashboard: () => api.get('/faculty/me/dashboard').then((r) => r.data.data),
  subjects: () => api.get('/faculty/me/subjects').then((r) => r.data.data),
  myTimetable: () => api.get('/faculty/me/timetable').then((r) => r.data.data),
  studentsForSubject: (subjectId) =>
    api.get(`/faculty/subjects/${subjectId}/students`).then((r) => r.data.data),
};
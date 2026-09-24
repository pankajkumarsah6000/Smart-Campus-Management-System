import api from './api';

export const attendanceService = {
  getMySummary: () => api.get('/attendance/me/summary').then((r) => r.data.data),
  getStudentSummary: (studentId) => api.get(`/attendance/student/${studentId}/summary`).then((r) => r.data.data),
  markAttendance: (payload) => api.post('/attendance/mark', payload).then((r) => r.data.data),
  getForSubjectAndDate: (subjectId, date) =>
    api.get(`/attendance/subject/${subjectId}`, { params: { date } }).then((r) => r.data.data),
};

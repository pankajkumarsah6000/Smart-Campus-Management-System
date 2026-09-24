import api from './api';

export const studentService = {
  getMyProfile: () => api.get('/students/me').then((r) => r.data.data),
  getMyDashboard: () => api.get('/students/me/dashboard').then((r) => r.data.data),
};

import api from './api';

export const authService = {
  async login(email, password) {
    const { data } = await api.post('/auth/login', { email, password });
    return data.data; // AuthResponse: accessToken, refreshToken, userId, email, roles...
  },
  async registerStudent(payload) {
    const { data } = await api.post('/auth/register', payload);
    return data.data;
  },
  async logout() {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.clear();
    }
  },
};

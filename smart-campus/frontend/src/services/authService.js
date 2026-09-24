import api from './api';

export const authService = {
  async login(email, password) {
    const { data } = await api.post('/auth/login', { email, password });
    return data.data; // AuthResponse: accessToken, refreshToken, userId, email, roles...
  },
  async logout() {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.clear();
    }
  },
};

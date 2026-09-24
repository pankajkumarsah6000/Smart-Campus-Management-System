import axios from 'axios';

// Vite dev server proxies /api to the Spring Boot backend (see vite.config.js).
// In production, set VITE_API_BASE_URL to the deployed backend URL.
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let refreshQueue = [];

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry && originalRequest.url !== '/auth/refresh') {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        isRefreshing = false;
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post(`${baseURL}/auth/refresh`, { refreshToken });
        const newAccessToken = data.data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        refreshQueue.forEach(({ resolve }) => resolve(newAccessToken));
        refreshQueue = [];
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        refreshQueue.forEach(({ reject }) => reject(refreshError));
        refreshQueue = [];
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;

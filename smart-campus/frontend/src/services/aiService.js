import api from './api';

export const aiService = {
  chat: (message, sessionId) => api.post('/ai/chat', { message, sessionId }).then((r) => r.data.data),
};

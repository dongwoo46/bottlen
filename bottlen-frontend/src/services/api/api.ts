// services/api/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL + '/api' || 'http://localhost:8080/api',
  withCredentials: true, // 쿠키 기반 인증이면 true
});

// 요청 인터셉터
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // 자동 로그아웃 or 리프레시 처리 가능
      console.warn('인증 만료');
    }
    return Promise.reject(err);
  }
);

export default api;

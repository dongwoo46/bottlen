// OAuth 로그인 관련 API (Redirect URL 생성 전용)
const backendBase = import.meta.env.VITE_API_BASE_URL as string;

export const oauthApi = {
  buildAuthUrl: (provider: 'kakao' | 'naver' | 'google') => {
    return `${backendBase}/oauth2/authorization/${provider}`;
  },
};

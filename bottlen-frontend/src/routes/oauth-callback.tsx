import { useEffect } from 'react';
import { useNavigate, createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/oauth-callback')({
  component: OAuthCallback,
});

export default function OAuthCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const redirectTo = params.get('redirectTo') || '/';

    if (token) {
      // 토큰 저장
      localStorage.setItem('token', token);

      // 원하는 페이지로 이동
      navigate({ to: redirectTo });
    }
  }, [navigate]);

  return <div>로그인 처리 중...</div>;
}

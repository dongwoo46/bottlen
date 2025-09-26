import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/auth/kakao/callback')({
  component: () => <div>카카오 로그인 처리 중...</div>,
});

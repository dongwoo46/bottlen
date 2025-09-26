import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/auth/naver/callback')({
  component: () => <div>네이버 로그인 처리 중...</div>,
});

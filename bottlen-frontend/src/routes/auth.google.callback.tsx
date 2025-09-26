import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/auth/google/callback')({
  component: () => <div>구글 로그인 처리 중...</div>,
});

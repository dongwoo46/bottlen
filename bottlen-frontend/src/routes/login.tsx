import { createFileRoute } from '@tanstack/react-router';
import OAuthButton from '../components/OAuthButton';

export const Route = createFileRoute()({
  component: () => (
    <div className="mx-auto w-full max-w-sm space-y-3">
      <h1 className="text-xl font-bold">로그인</h1>
      <OAuthButton provider="kakao" size="md" />
      <OAuthButton provider="naver" size="md" />
      <OAuthButton provider="google" size="md" />
    </div>
  ),
});

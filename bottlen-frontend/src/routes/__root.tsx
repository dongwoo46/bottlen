import { Outlet, Link } from '@tanstack/react-router';
import { createRootRoute } from '@tanstack/react-router';

export const Route = createRootRoute({
  component: () => (
    <div className="min-h-screen flex flex-col">
      <header className="bg-blue-600 text-white p-4 flex gap-4">
        <Link to="/">홈</Link>
        <Link to="/login">로그인</Link>
      </header>
      <main className="flex-1 p-6 bg-gray-50">
        <Outlet />
      </main>
    </div>
  ),
});

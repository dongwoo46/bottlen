import { useState } from 'react';
import { createFileRoute } from '@tanstack/react-router';
import api from '../services/api/api';

export const Route = createFileRoute('/')({
  component: Home,
});

function Home() {
  const [userInfo, setUserInfo] = useState<any | null>(null);

  const fetchUserInfo = async () => {
    try {
      const res = await api.get('/users/me'); // ✅ JWT 인증 헤더 자동 포함
      setUserInfo(res.data);
    } catch (err) {
      console.error('내 정보 조회 실패:', err);
      alert('로그인이 필요합니다.');
    }
  };

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold">홈 화면</h1>

      {/* 로그인 버튼 */}
      <div className="mt-4">
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          className="px-4 py-2 bg-blue-500 text-white rounded"
        >
          구글 로그인
        </a>
      </div>

      {/* 내 정보 조회 버튼 */}
      <div className="mt-4">
        <button
          onClick={fetchUserInfo}
          className="px-4 py-2 bg-green-500 text-white rounded"
        >
          내 정보 조회
        </button>
      </div>

      {/* 사용자 정보 표시 */}
      {userInfo && (
        <div className="mt-4 border p-4 rounded bg-gray-100">
          <h2 className="font-semibold">내 정보</h2>
          <p>
            <strong>ID:</strong> {userInfo.id}
          </p>
          <p>
            <strong>Email:</strong> {userInfo.email}
          </p>
          <p>
            <strong>닉네임:</strong> {userInfo.nickname}
          </p>
          <p>
            <strong>역할:</strong> {userInfo.role}
          </p>
        </div>
      )}
    </div>
  );
}

export default Home;

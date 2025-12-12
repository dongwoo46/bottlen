import { useState, useEffect } from 'react';
import { createFileRoute, useNavigate } from '@tanstack/react-router';
import api from '../services/api/api';

export const Route = createFileRoute('/signup-extra')({
  component: SignupExtra,
});

function SignupExtra() {
  const [nickname, setNickname] = useState('');
  const navigate = useNavigate();

  // 1) URL 파라미터에서 token 추출 → localStorage 저장
  useEffect(() => {
    const url = new URL(window.location.href);
    const token = url.searchParams.get('token');
    if (token) {
      localStorage.setItem('accessToken', token);
    }
  }, []);

  // 2) 회원가입 추가 정보 저장 요청
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/users/complete-signup', { nickname });
      alert('추가 정보 저장 완료!');
      navigate({ to: '/' }); // ✅ 라우터 네비게이션 사용
    } catch (err) {
      console.error(err);
      alert('저장 실패');
    }
  };

  return (
    <div>
      <h1>추가 정보 입력</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>닉네임</label>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
          />
        </div>
        <button type="submit">가입 완료</button>
      </form>
    </div>
  );
}

export default SignupExtra;

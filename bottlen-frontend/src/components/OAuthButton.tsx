import { oauthApi } from '../services/api/oauth-api';

type Provider = 'kakao' | 'naver' | 'google';
type Size = 'sm' | 'md' | 'lg';

interface Props {
  provider: Provider;
  size?: Size;
  className?: string;
}

const SIZE_CLASS: Record<Size, string> = {
  sm: 'h-10 max-w-xs',
  md: 'h-12 max-w-sm',
  lg: 'h-14 max-w-md',
};

const IMG_URL: Record<Provider, string> = {
  kakao:
    'https://developers.kakao.com/tool/resource/static/img/button/login/full/ko/kakao_login_large_narrow.png',
  naver: 'https://static.nid.naver.com/oauth/small_g_in.PNG',
  google:
    'https://developers.google.com/identity/images/btn_google_signin_light_normal_web.png',
};

const ALT: Record<Provider, string> = {
  kakao: '카카오 로그인',
  naver: '네이버 로그인',
  google: '구글 로그인',
};

export default function OAuthButton({
  provider,
  size = 'md',
  className,
}: Props) {
  const href = oauthApi.buildAuthUrl(provider);

  const src = IMG_URL[provider];
  const alt = ALT[provider];

  return (
    <a href={href} aria-label={alt} className={`block ${className ?? ''}`}>
      <div className={`w-full ${SIZE_CLASS[size]} rounded-md overflow-hidden`}>
        <div className="w-full h-full flex items-center justify-center">
          <img
            src={src}
            alt={alt}
            className="h-full w-full object-contain select-none"
            loading="lazy"
            referrerPolicy="no-referrer"
            draggable={false}
          />
        </div>
      </div>
    </a>
  );
}

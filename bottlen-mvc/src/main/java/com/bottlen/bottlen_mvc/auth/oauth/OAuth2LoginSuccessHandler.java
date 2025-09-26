package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

// OAuth2 로그인 성공 직후 실행되는 후처리 로직을 담당
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // OAuth2User → CustomOAuth2User
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();

        // dto에서 email, globalId 가져오기
        String email = customUser.getEmail();
        String globalId = customUser.getProvider() + ":" + customUser.getProviderId();

        // 권한(role) 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.isEmpty()
                ? "ROLE_USER"
                : authorities.iterator().next().getAuthority();

        // JWT 생성
        String token = jwtUtil.createJwt(
                email,
                globalId,
                role,
                60 * 60 * 60L // 만료 시간 (예: 60시간)
        );

        // JWT를 HttpOnly 쿠키로 저장
        response.addCookie(createCookie("Authorization", token));

        // 항상 특정 URL로 리다이렉트하도록 지정
        setAlwaysUseDefaultTargetUrl(true);
        setDefaultTargetUrl("http://localhost:5173/");

        // 부모 클래스 메서드 호출 → RedirectStrategy가 실행됨
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        //https일때 사용
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
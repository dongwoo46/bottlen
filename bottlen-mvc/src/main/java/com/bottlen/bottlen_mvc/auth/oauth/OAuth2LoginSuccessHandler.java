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

        String email = customUser.getEmail();
        String globalId = customUser.getProvider() + ":" + customUser.getProviderId();
        Long userId = customUser.getUserId();

        // 권한(role) 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.isEmpty()
                ? "ROLE_USER"
                : authorities.iterator().next().getAuthority();

        // JWT 생성
        String token = jwtUtil.createJwt(
                userId,   // userId claim으로 추가
                email,
                globalId,
                role,
                60 * 60 * 1000L // 1시간 (3600초 → 3600 * 1000 밀리초)
        );

        // 리다이렉트 경로 결정
        String redirectTo = customUser.isNewUser() ? "/signup-extra" : "/";

        // 무조건 oauth-callback으로 보내고, token + redirectTo 전달
        getRedirectStrategy().sendRedirect(
                request,
                response,
                "http://localhost:5173/oauth-callback?token=" + token + "&redirectTo=" + redirectTo
        );
    }
}

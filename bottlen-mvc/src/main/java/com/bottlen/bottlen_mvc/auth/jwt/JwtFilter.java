package com.bottlen.bottlen_mvc.auth.jwt;

import com.bottlen.bottlen_mvc.auth.oauth.CustomOAuth2User;
import com.bottlen.bottlen_mvc.auth.oauth.dto.OAuthUserDto;
import com.bottlen.bottlen_mvc.user.domain.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // cookie들 중 Authorization key 찾기
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        // Authorization 쿠키 없으면 다음 필터 진행
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인
        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 값 추출
        String email = jwtUtil.getEmail(token);
        String globalId = jwtUtil.getGlobalId(token); // provider_providerId
        String role = jwtUtil.getRole(token);

        // OAuthUserDto 생성 (이 DTO는 DB에서 조회/저장된 사용자 정보와 매핑)
        OAuthUserDto userDto = OAuthUserDto.builder()
                .email(email)
                .globalId(globalId)
                .role(Role.valueOf(role)) // String → Enum 변환
                .build();

        // CustomOAuth2User (UserDetails 역할) 생성
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);

        // 스프링 시큐리티 인증 토큰 생성 & 등록
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                customOAuth2User.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

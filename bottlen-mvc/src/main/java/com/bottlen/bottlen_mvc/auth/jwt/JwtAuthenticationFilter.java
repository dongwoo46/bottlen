package com.bottlen.bottlen_mvc.auth.jwt;

import com.bottlen.bottlen_mvc.auth.oauth.CustomOAuth2User;
import com.bottlen.bottlen_mvc.auth.oauth.dto.OAuthUserDto;
import com.bottlen.bottlen_mvc.user.domain.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 토큰 추출
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response); // 토큰 없으면 그냥 넘김
                return;
            }

            String token = authHeader.substring(7); // "Bearer " 제거

            // 2. JWT 검증 및 유저 정보 추출
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String email = jwtUtil.getEmail(token);
                String globalId = jwtUtil.getGlobalId(token);
                String role = jwtUtil.getRole(token);

                // 3. OAuthUserDto 생성
                OAuthUserDto userDto = OAuthUserDto.builder()
                        .userId(userId)
                        .email(email)
                        .globalId(globalId)
                        .role(Role.valueOf(role))
                        .build();

                // 4. CustomOAuth2User 생성 (UserDetails 구현체)
                CustomOAuth2User customUser = new CustomOAuth2User(userDto);

                // 5. 인증 토큰 생성 및 SecurityContext 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                customUser,
                                null,
                                customUser.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            handleException(response, "토큰이 만료되었습니다.", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (io.jsonwebtoken.JwtException e) { // 다른 JWT 예외
            handleException(response, "유효하지 않은 토큰입니다.", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

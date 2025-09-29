package com.bottlen.bottlen_mvc.config;

import com.bottlen.bottlen_mvc.auth.jwt.JwtAuthenticationFilter;
import com.bottlen.bottlen_mvc.auth.oauth.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // 주입

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // CSRF 비활성화 (REST API 용도)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Spring MVC의 CorsConfigurationSource를 따르게 함


                // 기본 로그인/로그아웃/Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())

                // OAuth2 로그인 → custom successHandler 사용
                .oauth2Login(oauth -> oauth.successHandler(oAuth2LoginSuccessHandler))

                // 경로별 인가 정책
                .authorizeHttpRequests(auth -> auth
                        // Preflight 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 추가정보 입력은 공개
                        .requestMatchers("/api/users/complete-signup").permitAll()
                        // 홈/기타 공개 리소스 허용
                        .requestMatchers("/", "/health", "/public/**").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 세션은 STATELESS (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 실패 시 → 로그인 폼 redirect 대신 401 반환
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // JwtAuthenticationFilter 등록 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


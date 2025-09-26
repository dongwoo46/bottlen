package com.bottlen.bottlen_mvc.auth.oauth.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuthController {

    // 카카오 로그인 시작
    @GetMapping("/oauth2/authorization/kakao")
    public String kakaoLogin() {
        return "redirect:/oauth2/authorization/kakao";
    }

    // 네이버 로그인 시작
    @GetMapping("/oauth2/authorization/naver")
    public String naverLogin() {
        return "redirect:/oauth2/authorization/naver";
    }

    // 구글 로그인 시작
    @GetMapping("/oauth2/authorization/google")
    public String googleLogin() {
        return "redirect:/oauth2/authorization/google";
    }

    // 공통 콜백 처리 (/login/oauth2/code/{provider} 로 spring-security가 자동 핸들링)
}
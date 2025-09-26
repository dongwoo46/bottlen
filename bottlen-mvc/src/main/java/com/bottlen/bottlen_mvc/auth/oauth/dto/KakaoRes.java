package com.bottlen.bottlen_mvc.auth.oauth.dto;

import java.util.Map;

public class KakaoRes implements OAuth2Res {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoRes(Map<String, Object> attributes) {
        this.attributes = attributes;

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new IllegalArgumentException("kakao_account 정보가 없습니다.");
        }

        Object emailObj = kakaoAccount.get("email");
        if (emailObj == null) {
            throw new IllegalArgumentException("이메일 정보가 없습니다. Kakao OAuth 로그인 실패");
        }

        this.kakaoAccount = kakaoAccount;

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        this.profile = profile != null ? profile : Map.of();  // null일 경우 빈 map
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getName() {
        return profile.getOrDefault("nickname", "unknown").toString();  // nickname이 없을 수도 있음
    }

    @Override
    public String getPhoneNumber() {
        Object phone = kakaoAccount.get("phone_number");
        return phone != null ? phone.toString() : null;
    }
}

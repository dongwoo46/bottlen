package com.bottlen.bottlen_mvc.auth.oauth.dto;

import java.util.Map;

public class NaverRes implements OAuth2Res{

    private final Map<String, Object> attribute;

    public NaverRes(Map<String, Object> attribute) {
        // 캐스팅을 이용해 객체 타입을 map으로 바꿔서 집어넣어줌
        Map<String, Object> response = (Map<String, Object>) attribute.get("response");
        if (response.get("email") == null) {
            throw new IllegalArgumentException("이메일 정보가 없습니다.");
        }
        this.attribute = response;
    }

    @Override
    public String getProvider() {

        return "naver";
    }

    @Override
    public String getProviderId() {

        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {

        return attribute.get("name").toString();
    }

    @Override
    public String getPhoneNumber() {
        return attribute.get("phone").toString();
    }
}

package com.bottlen.bottlen_mvc.auth.oauth.dto;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleRes implements OAuth2Res{

    private final Map<String, Object> attribute;

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
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

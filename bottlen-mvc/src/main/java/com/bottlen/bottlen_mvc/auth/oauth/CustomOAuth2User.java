package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.oauth.dto.OAuth2Res;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * - Spring Security가 요구하는 OAuth2User를 구현
 * - 우리 서비스 도메인에 필요한 필드( provider, providerId, email, name, picture, userId )를 보유
 * - SecurityContext에 UsernamePrincipal로 올라가는 객체
 */

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Res oAuth2Res;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(OAuth2Res oAuth2Res, Map<String, Object> attributes) {
        this.oAuth2Res = oAuth2Res;
        this.attributes = attributes;
    }

    public String getProvider() {
        return oAuth2Res.getProvider();
    }

    public String getProviderId() {
        return oAuth2Res.getProviderId();
    }

    public String getEmail() {
        return oAuth2Res.getEmail();
    }
    
    // 아래 3가지는 OAuth2User에 있는 추상 메서드기 때문에 오버라이드
    @Override
    public String getName() {
        return oAuth2Res.getName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // 나중에 ROLE 매핑
    }


}

package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.oauth.dto.OAuthUserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * - Spring Security가 요구하는 OAuth2User를 구현
 * - 우리 서비스 도메인에 필요한 필드(provider, providerId, email, role, isNewUser)를 보유
 * - SecurityContext에 Principal로 올라가는 객체
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuthUserDto dto;

    public CustomOAuth2User(OAuthUserDto dto) {
        this.dto = dto;
    }

    public String getProvider() {
        return dto.getProvider();
    }

    public String getProviderId() {
        return dto.getProviderId();
    }

    public String getGlobalId() {
        return dto.getGlobalId();
    }

    public String getEmail() {
        return dto.getEmail();
    }

    public String getRole() {
        return dto.getRole().name();
    }

    public boolean isNewUser() {
        return dto.isNewUser();
    }

    public boolean isPhoneUnverified() {
        return dto.isPhoneUnverified();
    }

    // 닉네임은 추가정보 입력 후에만 값이 있을 수 있음
    public String getNickname() {
        return dto.getNickname();
    }

    public Long getUserId() {
        return dto.getUserId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        // 필요하면 OAuth provider attribute를 담을 수 있도록 확장 가능
        return null;
    }

    @Override
    public String getName() {
        // 닉네임이 없으면 email로 대체
        return dto.getNickname() != null ? dto.getNickname() : dto.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> dto.getRole().name());
    }
}

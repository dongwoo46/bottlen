package com.bottlen.bottlen_mvc.auth.oauth.dto;


import com.bottlen.bottlen_mvc.user.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthUserDto {
    private String provider;     // kakao, google, naver
    private String providerId;   // 각 서비스에서 내려준 id
    private String globalId; //provider_providerId
    private String email;
    private String nickname;
    private Role role;         // ROLE_USER 기본값
}

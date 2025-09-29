package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.oauth.dto.*;
import com.bottlen.bottlen_mvc.infrastructure.redis.RedisService;
import com.bottlen.bottlen_mvc.user.domain.AuthProvider;
import com.bottlen.bottlen_mvc.user.domain.Role;
import com.bottlen.bottlen_mvc.user.domain.User;
import com.bottlen.bottlen_mvc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RedisService redisService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1) Provider에서 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Res oAuth2Response = switch (registrationId) {
            case "naver" -> new NaverRes(oAuth2User.getAttributes());
            case "google" -> new GoogleRes(oAuth2User.getAttributes());
            case "kakao" -> new KakaoRes(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };

        // 2) OAuth2 응답 기반 DTO 생성
        OAuthUserDto dto = OAuthUserDto.builder()
                .provider(oAuth2Response.getProvider())
                .providerId(oAuth2Response.getProviderId())
                .email(oAuth2Response.getEmail())
                .role(Role.ROLE_USER)
                .build();

        // 3) provider + providerId 기준으로 유저 조회
        User user = userRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(dto.getProvider().toUpperCase()),
                dto.getProviderId()
        ).orElse(null);

        boolean isNewUser;
//        boolean isPhoneUnverified;

        if (user == null) {
            // 신규 유저 → phone 없음
            user = User.createOAuthUser(
                    AuthProvider.valueOf(dto.getProvider().toUpperCase()),
                    dto.getProviderId(),
                    dto.getEmail()
            );
            isNewUser = true;
//            isPhoneUnverified = true; // 신규면 당연히 phone 없음
        } else {
            // 기존 유저 → 이메일 갱신
            user.updateEmail(dto.getEmail());
            isNewUser = false;
            // 기존 유저라도 phone이 비어있으면 미인증 상태
//            isPhoneUnverified = (user.getPhone() == null);
        }

        // 4) DB 저장
        userRepository.save(user);

        // 5) DTO에 상태 세팅
        dto.setNewUser(isNewUser);
//        dto.setPhoneUnverified(isPhoneUnverified);
        dto.setUserId(user.getId()); // PK 넣어주기

        // 신규 유저 or 전화번호 미등록 → Redis에 임시 저장 (5분 TTL)
        if (isNewUser) {
            redisService.setValueWithTTL("signup:" + user.getId(), dto, 300);
        }

        return new CustomOAuth2User(dto);
    }
}

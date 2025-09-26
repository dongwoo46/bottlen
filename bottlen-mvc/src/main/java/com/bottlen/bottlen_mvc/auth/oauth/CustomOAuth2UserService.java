package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.oauth.dto.*;
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


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("OAuth2User attributes: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Res oAuth2Response;

        switch (registrationId) {
            case "naver" -> oAuth2Response = new NaverRes(oAuth2User.getAttributes());
            case "google" -> oAuth2Response = new GoogleRes(oAuth2User.getAttributes());
            case "kakao" -> oAuth2Response = new KakaoRes(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // DTO 생성
        OAuthUserDto dto = OAuthUserDto.builder()
                .provider(oAuth2Response.getProvider())
                .providerId(oAuth2Response.getProviderId())
                .email(oAuth2Response.getEmail())
                .nickname(oAuth2Response.getName())
                .role(Role.ROLE_USER)
                .build();

        // DB 저장 or 업데이트
        User user = userRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(dto.getProvider().toUpperCase()),
                dto.getProviderId()
        ).map(existing -> {
            // 이미 있는 유저 → 프로필 갱신
            existing.updateProfile(dto.getEmail(), dto.getNickname());
            return existing;
        }).orElseGet(() ->
                // 신규 유저 생성
                User.createOAuthUser(
                        AuthProvider.valueOf(dto.getProvider().toUpperCase()),
                        dto.getProviderId(),
                        dto.getEmail(),
                        dto.getNickname()
                )
        );

        userRepository.save(user);

        // SecurityContext에 등록할 객체 반환
        return new CustomOAuth2User(dto);

    }
}

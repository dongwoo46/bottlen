package com.bottlen.bottlen_mvc.auth.oauth;

import com.bottlen.bottlen_mvc.auth.oauth.dto.GoogleRes;
import com.bottlen.bottlen_mvc.auth.oauth.dto.NaverRes;
import com.bottlen.bottlen_mvc.auth.oauth.dto.KakaoRes;
import com.bottlen.bottlen_mvc.auth.oauth.dto.OAuth2Res;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

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

        // 우리가 만든 CustomOAuth2User로 감싸서 반환
        return new CustomOAuth2User(oAuth2Response, oAuth2User.getAttributes());
    }
}

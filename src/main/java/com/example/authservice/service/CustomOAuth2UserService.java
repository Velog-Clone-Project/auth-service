package com.example.authservice.service;

import com.example.authservice.dto.KakaoResponse;
import com.example.authservice.dto.OAuth2Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
// OAuth2 로그인 시 사용자 정보를 커스터마이징하여 처리하는 서비스
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 제공(OAuth2UserRequest)되는 OAuth2 사용자 정보를 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

//        // 사용자 정보 JSON 형태로 확인
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        System.out.println("Kakao 사용자 정보: " + attributes);

        // 공금자 확인(ex: kakao, google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> enrichedAttributes = getEnrichedUserAttributes(registrationId, oAuth2User);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_SOCIAL_USER")), // 권한 설정
                enrichedAttributes, // 사용자 정보 Map
                "email"  // OAuth2User의 getName() 메서드에서 사용할 기본 키
        );
    }

    // 기본 사용자 정보에 추가적으로 필요한 값들을 삽입하는 메서드
    private static Map<String, Object> getEnrichedUserAttributes(String registrationId, OAuth2User oAuth2User) {

        OAuth2Response oAuth2Response;

        // 카카오 로그인일 경우, KakaoResponse 객체로 래핑
        if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            // 지원하지 않는 소셜 로그인 제공자일 경우 예외 발생
            throw new OAuth2AuthenticationException("Unsupported registration id: " + registrationId);
        }

        // 기본 사용자 정보에 추가적으로 필요한 값들 삽입
        Map<String, Object> enrichedAttributes = new HashMap<>(oAuth2User.getAttributes());
        enrichedAttributes.put("email", oAuth2Response.getEmail());
        enrichedAttributes.put("userId", oAuth2Response.getUserId());
        enrichedAttributes.put("profileName", oAuth2Response.getProfileName());
        enrichedAttributes.put("profileImageUrl", oAuth2Response.getProfileImageUrl());
        enrichedAttributes.put("provider", oAuth2Response.getProvider());

        // 사용자 정보를 담은 Map 반환
        return enrichedAttributes;
    }
}

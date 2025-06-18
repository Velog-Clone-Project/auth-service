package com.example.authservice.handler;

import com.example.authservice.domain.UserEntity;
import com.example.authservice.domain.UserType;
import com.example.authservice.exception.auth.GeneralAccountLoginOnlyException;
import com.example.authservice.jwt.JwtTokenProvider;
import com.example.authservice.repository.AuthRepository;
import com.example.authservice.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
// final 필드들을 생성자 주입 방식으로 처리
@RequiredArgsConstructor
// OAuth2 로그인 성공 시 처리하는 핸들러
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    // 사용자 존재 여부 확인을 위한 리포지토리
    private final AuthRepository authRepository;
    // JWT 생성 유틸
    private final JwtTokenProvider jwtTokenProvider;
    // refresh token 저장용 Redis 서비스
    private final RedisService redisService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // OAuth2 인증 정보에서 사용자 정보를 가져옴
        // CustomOAuth2UserService.loadUser()에서 반환된 객체: authentication.getPrincipal()
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        String provider = (String) oAuth2User.getAttributes().get("provider");
        String profileName = (String) oAuth2User.getAttributes().get("profileName");
        // TODO: profileImageUrl은 왜 사용안하지?
        // 신규 회원의 소셜 로그인 흐름
        // 1. 소셜 로그인
        // 2. 보류 -> 회원가입에 필요한 정보 전달
        // 3. 회원가입 신청 후 백엔드에서 처리
        // 4. 사용자 생성하면서 user-service에 이벤트 발행
        // 5. userId, profileName, profileImageUrl, bio 등의 정보를 함께 전달
        // 6. user-service에서 해당 정보를 바탕으로 유저 생성
        String profileImageUrl = (String) oAuth2User.getAttributes().get("profileImageUrl");
        String oauthUserId = (String) oAuth2User.getAttributes().get("userId");

        // 기존 회원 여부 확인
        Optional<UserEntity> userOpt = authRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();

            if (!user.getUserType().equals(UserType.valueOf(provider.toUpperCase()))) {
                throw new GeneralAccountLoginOnlyException();
            }

            // 기존 회원일 경우: refresh token  발급 및 Redis 저장 후 쿠키로 전송
            String userId = userOpt.get().getUserId();

            String refreshToken = jwtTokenProvider.createRefreshToken(userId);
            redisService.storeRefreshToken(userId, refreshToken);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
            response.addCookie(refreshTokenCookie);
        } else {
            // 신규 회원일 경우: 필요한 정보를 쿠키로 전달해 프론트에서 회원가입 진행
            response.addCookie(newCookie("email", email));
            response.addCookie(newCookie("userId", oauthUserId));
            response.addCookie(newCookie("profileName", profileName));
            response.addCookie(newCookie("provider", provider));
        }

        // 프론트엔드는 이 경로(/auth/oauth2-redirect)에서 쿠키를 읽고 후속 처리 (회원가입 or 토큰 재요청)
        // TODO: 해당 URL은 실제 프론트엔드 주소로 수정 필요
        response.sendRedirect("http://front.server.url/auth/oauth2-redirect");
    }

    // 쿠키 생성 메서드
    private Cookie newCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        return cookie;
    }
}

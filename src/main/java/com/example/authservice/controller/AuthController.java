package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.exception.cookie.NoRefreshTokenCookieException;
import com.example.authservice.exception.cookie.RefreshTokenNotFoundException;
import com.example.authservice.service.AuthService;
import com.example.common.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

// REST API용 컨트롤러임을 명시
// @Controller + @ResponseBody
@RestController
// 모든 엔드포인트는 /auth 로 시작
@RequestMapping("/auth")
// final 필드인 authService를 생성자 주입
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // application 설정파일에서 토큰 만료시간 로드
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 일반 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            // @Valid 어노테이션을 통해 요청 본문에서 회원가입 정보 검증(DTO 클래스에 유효성 검사 어노테이션이 있어야 함)
            @Valid @RequestBody SignupRequestDto request,
            // response 객체를 통해 쿠키를 설정하기 위해 HttpServletResponse를 주입받음
            HttpServletResponse response) {

        // 유저 생성
        // 회원가입 요청을 처리하고 응답 DTO를 생성
        SignupResponseDto signupResponse = authService.createUser(request);

        // 응답에 refreshToken 쿠키를 추가
        response.addCookie(setRefreshTokenCookie(signupResponse.getRefreshToken(),
                (int) (refreshTokenExpiration / 1000)));

        return ResponseEntity
                .status(HttpStatus.CREATED)         // 201 Created 상태 코드
                .body(new ApiResponse<>("Signup successful", signupResponse));
    }


    // 소셜 회원가입
    @PostMapping("/signup/social")
    public ResponseEntity<ApiResponse<SignupResponseDto>> socialSignup(
            // @Valid 어노테이션을 통해 요청 본문에서 회원가입 정보 검증(DTO 클래스에 유효성 검사 어노테이션이 있어야 함)
            @Valid @RequestBody SocialSignupRequestDto request,
            // response 객체를 통해 쿠키를 설정하기 위해 HttpServletResponse를 주입받음
            HttpServletResponse response) {

        // 소셜 유저 생성
        SignupResponseDto signupResponse = authService.createSocialUser(request);

        response.addCookie(setRefreshTokenCookie(signupResponse.getRefreshToken(),
                (int) (refreshTokenExpiration / 1000)));

        return ResponseEntity
                .status(HttpStatus.CREATED)         // 201 Created 상태 코드
                .body(new ApiResponse<>("Social signup successful", signupResponse));
    }


    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LoginResponseDto loginResponse = authService.authenticateUser(request);

        response.addCookie(setRefreshTokenCookie(loginResponse.getRefreshToken(),
                (int) (refreshTokenExpiration / 1000)));

        return ResponseEntity.ok(new ApiResponse<>("Login successful", loginResponse));
    }

    // Access Token 재발급
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissueAccessToken(
            HttpServletRequest request, HttpServletResponse response
    ) {
        // refreshToken을 쿠키에서 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        // refreshToken 검증 및 사용자 식별 후 토큰 발급
        TokenResponseDto tokenResponse = authService.reissueTokens(refreshToken);

        // 쿠키에 새로 발급된 refreshToken 저장
        response.addCookie(setRefreshTokenCookie(tokenResponse.getRefreshToken(),
                (int) (refreshTokenExpiration / 1000)));

        return ResponseEntity
                .ok(new ApiResponse<>("Access token has been reissued successfully", tokenResponse));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("X-User-Id") String userId,
            HttpServletResponse response) {

        authService.logout(userId);

        // 클라이언트에 있는 refreshToken 쿠키 제거
        // maxAge를 0으로 설정하여 쿠키를 즉시 만료시킴
        response.addCookie(setRefreshTokenCookie(null, 0));

        return ResponseEntity.ok(Map.of("message", "Logout successful"));

    }


//    // 회원 탈퇴
//    @DeleteMapping("/withdraw")
//    public ResponseEntity<Map<String, String>> withdraw(
//            @RequestHeader("X-User-Id") String userId,
//            HttpServletResponse response) {
//
//        authService.withdrawUser(userId);
//
//        // 클라이언트에 있는 refreshToken 쿠키 제거
//        response.addCookie(setRefreshTokenCookie(null, 0));
//
//        return ResponseEntity.ok(Map.of("message", "Account deletion completed successfully"));
//
//    }

    // 쿠키에 refreshToken을 설정하는 메서드
    private Cookie setRefreshTokenCookie(String refreshToken, int maxAge) {

        // TODO: ResponseCookie, Cookie 차이
        // ResponseCookie를 사용하여 쿠키 설정
//                ResponseCookie cookie = ResponseCookie.from("refreshToken", signupResponse.getRefreshToken())
//                .httpOnly(true)
//                .secure(true)                     // HTTPS 환경에서만 전송
//                .path("/")
//                .maxAge(refreshTokenExpiration / 1000)      // TTL
//                .sameSite("Lax")                // SameSite=Lax, Strict, None 중 택,
//                .build();
//
//        response.addHeader("Set-Cookie", cookie.toString());

        // refreshToken을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);       // HttpOnly 속성으로 클라이언트 측 스크립트에서 접근 불가
        refreshTokenCookie.setPath("/");            // 쿠키의 유효 경로 설정, 모든 경로에서 접근 가능
        refreshTokenCookie.setMaxAge(maxAge);       // 쿠키의 유효 기간 설정 (초 단위)
//        refreshTokenCookie.setSecure(true);       // HTTPS 환경에서만 전송되도록 설정

        return refreshTokenCookie;
    }

    // 요청 쿠키에서 refreshToken을 추출하는 메서드
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        // 요청에 포함된 쿠키 배열이 null 인 경우 (쿠키 자체가 없음)
        if (request.getCookies() == null) {
            throw  new NoRefreshTokenCookieException();
        }

        // 요청에 포함된 쿠키 배열을 스트림으로 변환
        return Arrays.stream(request.getCookies())
                // 쿠키 배열에서 refreshToken 쿠키를 찾음
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                // 첫번째로 찾은 "refreshToken" 쿠키를 Optional<Cookie>로 변환
                .findFirst()
                // 쿠키 객체에서 실제 토큰 값을 추출
                // Optional<Cookie>에서 Cookie 객체를 가져옴
                // cookie -> cookie.getValue()로 람다식의 메서드 참조로 변환
                .map(Cookie::getValue)
                // 없으면 예외 발생
                .orElseThrow(() -> new RefreshTokenNotFoundException());
    }
}
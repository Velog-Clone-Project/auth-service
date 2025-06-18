package com.example.authservice.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
// Lombok을 사용하여 로그 객체(log)를 자동 생성
@Slf4j
// OAuth2 로그인 실패 시 처리하는 핸들러
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    // 소셜 로그인 인증 실패 시 실행되는 콜백 메서드
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        // 로그에 실패 메시지 경고 레벨로 기록
        log.warn("social login fail: {}", exception.getMessage());

        // 오류 메시지를 포함한 리다이렉트 응답 전송
        // 1. 상태코드는 기본적으로 302 (redirect 시 자동 설정)
        // 2. 오류 메시지를 쿼리 파라미터로 login 페이지에 전달
        // TODO: 향후 /login을 프론트엔드 로그인 페이지로 변경 예정
        response.sendRedirect("/login?error=" + URLEncoder.encode("social login fail", "UTF-8"));
    }
}

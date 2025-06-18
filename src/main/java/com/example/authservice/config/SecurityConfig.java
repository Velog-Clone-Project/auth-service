package com.example.authservice.config;

import com.example.authservice.handler.CustomOAuth2FailureHandler;
import com.example.authservice.handler.CustomOAuth2SuccessHandler;
import com.example.authservice.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
// Spring Security 활성화
@EnableWebSecurity
// final 필드에 대한 생성자 주입을 위한 Lombok 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    // 사용자 정보를 처리할 커스텀 OAuth2 서비스
    private final CustomOAuth2UserService customOAuth2UserService;

    // OAuth2 로그인 성공 및 실패 핸들러
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    // 비밀번호 암호화를 위한 BCrypt 인코더 빈 등록
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /*
         CSRF disable ( Cross-Site Request Forgery 방지 기능 비활성화 )
         JWT, REST API 기반에서는 CSRF 공격을 방어할 필요가 없다.
         CSRF 공격은 브라우저가 세션/쿠키를 자동으로 전송할때 발생하는데,

         JWT는 인증정보를 자동으로 전송하지 않아 CSRF의 공격에 대한 전제가 사라지고 방어할 필요가 없어진다.

         REST API는 기본적으로 무상태(stateless) 구조인데, 클라이언트(보통 프론트엔드)가 토큰을 직접 들고 인증하므로
         세션이나 쿠키 기반 인증이 아니다 고로 CSRF 공격보다는 XSS나 토큰 탈취 방지가 더 중요하다.

         하지만 Refresh Token을 쿠키에 저장하고 사용하는 경우에는 CSRF 공격에 취약할 수 있다.
         TODO: 이 부분은 추후에 CSRF 공격 방어를 위해 쿠키에 CSRF 토큰을 추가하는 방법을 고려해야 한다.
        */
        http
                .csrf(auth -> auth.disable());

        // Form login disable ( 기존 form 기반 로그인 비활성화 )
        http
                .formLogin(auth -> auth.disable());

        // HTTP Basic authentication disable ( Authorization 헤더 기본 인증 비활성화 )
        http
                .httpBasic(auth -> auth.disable());

        //oauth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                // 사용자 정보를 처리할 커스텀 OAuth2 서비스 설정
                                userInfoEndpointConfig.userService(customOAuth2UserService))
                        // OAuth2 로그인 성공 및 실패 핸들러 설정
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler(customOAuth2FailureHandler)
                );

        // 경로별 인가 설정
        http
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/**").permitAll()
//                        .anyRequest().authenticated());
                        .anyRequest().permitAll());

        // h2-console 접근을 위해 X-Frame-Options 헤더 비활성화
        http
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        //세션을 stateless로 설정하여 세션 저장 없이 JWT 기반 인증만 사용
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 최종적으로 SecurityFilterChain 객체를 반환
        return http.build();
    }
}

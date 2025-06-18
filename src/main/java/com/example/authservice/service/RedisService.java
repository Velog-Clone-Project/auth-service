package com.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    // Redis에 문자열(String) 기반의 키(String)-값(String)을 저장하기 위한 RedisTemplate 주입
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Redis에서 refreshToken 키를 구분하기 위한 prefix 정의("RT:{userId}" 형태로 저장)
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    // userId로 refreshToken 저장
    public void storeRefreshToken(String userId, String refreshToken) {

        // (Key) RT:{userId}: (Value) refreshToken 형태로 Redis에 저장하고, 만료시간 설정
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                Duration.ofMillis(refreshTokenExpiration) // TTL 설정
        );
    }

    // userId로 refreshToken 조회
    public String getRefreshTokenByUserId(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // 특정 userId에 해당하는 refreshToken을 삭제
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    // 저장된 refreshToken과 전달된 refreshToken이 일치하는지 확인
    public boolean isRefreshTokenValid(String userId, String refreshToken) {

        // 저장된 토큰을 가져옴
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        // 저장된 토큰이 null인 경우, 즉 해당 userId에 대한 refreshToken이 존재하지 않는 경우와
        // 저장된 토큰과 전달된 토큰이 일치하는지 비교하여 반환
        return storedToken != null && storedToken.equals(refreshToken);
    }

}


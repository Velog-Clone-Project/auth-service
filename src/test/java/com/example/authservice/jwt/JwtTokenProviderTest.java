package com.example.authservice.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {

        // 테스트용 공개키/개인키 주입
        JwtKeyProperties keyProperties = new JwtKeyProperties();
        keyProperties.setPrivateKeyPem("""
                -----BEGIN PRIVATE KEY-----
                MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFt1bv7mYLaZ9G
                qbrqErypo0DX3YClDKezP+7BxChpO3pPQ65u4RGFmAaqAufWIXoEGjGdp73ljoKq
                VD0aeoHq/inHe1BZInCeZReh0cAY+lAxmPI53/LnBKN/JDeraKLykNd1fb0pZzRj
                ZrmOKfySaiQ6UwhA/JGYpUa7T2XSJEVHPjpNQ3hPAIoXn4BeVjztipuT/O4eYjez
                r5XFZ27DPYP7+bIEN0egdaIYmISQtlQcqFx5UGZ3bszREEKBgDUD/NwNP5EuQmn7
                E6Oo2I7BGAV2NpDu4207x0CVYxE9nQo74dHkFzgKCX2yLWzMTF6cscI2kc7pWMOV
                E7Sx82x1AgMBAAECggEAImeAzUdiomHCwX0PdWPFSI+L3QRCOnY1SE+7sCP+FDGx
                WtGgLNMlRWXq4vKWfQBeRxB3ycNCnAtsWU9HQrBGoymPA5NFq/z+M9HLhvEEyFPm
                WHsQy1lY3zJDcQBQX5dZo3Poyh0TKX039IQPjdloheHKZAvGx+grPbCk2js6sJIo
                tJFt0nlq9w6OUuFFIXAeZMcLGztSPQUAATVszLLEvdqMCaiyzHe25g4TrDxusptN
                1KMhCwvB9ejbYrl6EoWY5FV0wbWW3Iwz22KMW803jdK39xsVvzd1oCLIa4IgdnHg
                gWioVd4A8KmDYhi+ZklCsV2GEgt9wV4vLtKUYVd8gQKBgQDwPxWL9+lrcKJ6seV1
                LZXR3MCyC09yECt1rsT3rMpE5HAH24E53uRsmMJRxGRQhNyN06wcN27jr+h2RXCF
                3p6n4TQfIyg+bf8yBpmr4GCxwR75iaqM2cuTy4dkaRcPig7xsXie8iLghosNtd4p
                Mz7O09MIyulcTjRNjvCLElQojQKBgQDSrlFktIypRwgv3sbiRhgmWpD8bd1mrVgj
                TifPtDwnnI0mjKeZs8TRM/JPSg4QsHx4QZnM0w7sbasfx/IPJ/Y3hQ4IoPJnze6f
                2tTYH4g4V0pb43/I8mftBb7nnXgBgkvDKdt3ZosKl0h4hlGWaKmCnay45DcLJSfc
                XY5ktYjdiQKBgQDgO/EveA/skO+GF160lPyeKS3NwFLBgqgdqib7iNItpQ2Pt+gQ
                jKKlXZsZWHXM1YVJ9wRQPckJSNlu8ncI6/UnekH8wgLA6kfjeFecHcwCfN8dR8ng
                tzOU2cWgCZpVjaVPHzcXG6dD6zAEdvhJeiQoRXtWXJWc6v1UWpLXKx9TzQKBgATU
                6Jr1pRAwx9hJNKh10RH4G8EeR+iV6yKMJU+XI5YI+he7PBDDVa0ykskMurvZQFGF
                t6BB6uKzNSx6amu8j+IAFC1Qe56z01gpLZ5zQq2AGv12fzS9knKWIiWvr9PhCBnG
                uPjJCkFI7OmOswpPBiwvr4lh+n0v8fUU1DItE6F5AoGAGMuNsP8JTh3sQNhKI41g
                a7SEURGxQti5pCqpJpDhhlgZGMWi6AQVS5XFQ67iq5KKEDaxhDtiwM9iHGhh6Zrq
                ld0OTEbw9OLnIvJkJ951bu+L4xcslSQJl/42UlWBnBL/etiDhbgLWn2ODvRnVjTH
                CTdlsN1z+cOV5fPqyZkKGnQ=
                -----END PRIVATE KEY-----
                """);
        keyProperties.setPublicKeyPem("""
                -----BEGIN PUBLIC KEY-----
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxbdW7+5mC2mfRqm66hK8
                qaNA192ApQynsz/uwcQoaTt6T0OubuERhZgGqgLn1iF6BBoxnae95Y6CqlQ9GnqB
                6v4px3tQWSJwnmUXodHAGPpQMZjyOd/y5wSjfyQ3q2ii8pDXdX29KWc0Y2a5jin8
                kmokOlMIQPyRmKVGu09l0iRFRz46TUN4TwCKF5+AXlY87Yqbk/zuHmI3s6+VxWdu
                wz2D+/myBDdHoHWiGJiEkLZUHKhceVBmd27M0RBCgYA1A/zcDT+RLkJp+xOjqNiO
                wRgFdjaQ7uNtO8dAlWMRPZ0KO+HR5Bc4Cgl9si1szExenLHCNpHO6VjDlRO0sfNs
                dQIDAQAB
                -----END PUBLIC KEY-----
                """);
        keyProperties.initKeys();

        jwtTokenProvider = new JwtTokenProvider(
                keyProperties,
                1000 * 60 * 15,  // accessToken 15분
                1000 * 60 * 60 * 24 * 7  // refreshToken 7일
        );
    }

    @DisplayName("AccessToken 생성 및 파싱 성공")
    @Test
    void shouldCreateAndParseAccessTokenSuccessfully() {

        // given
        String userId = "hong123";

        // when
        String token = jwtTokenProvider.createAccessToken(userId);
        String extractedUserId = jwtTokenProvider.getUserId(token);

        // then
        assertEquals(userId, extractedUserId);
        assertEquals(TokenValidationResult.VALID, jwtTokenProvider.validateToken(token));
    }

    @DisplayName("RefreshToken 생성 시 토큰 타입이 'refresh'로 설정되어야 한다")
    @Test
    void shouldCreateRefreshTokenWithCorrectType() {

        // given
        String userId = "user123";

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        Claims claims = Jwts.parser()
                .verifyWith(jwtTokenProvider.getPublicKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        // then
        assertEquals("refresh", claims.get("type"));
        assertEquals(userId, claims.getSubject());
    }

    @DisplayName("만료된 토큰 검증 실패")
    @Test
    void shouldFailToValidateExpiredToken() {

        // given
        String userId = "expiredUser";
        String expiredToken = Jwts.builder()
                .subject(userId)
                .expiration(Date.from(Instant.now().minusSeconds(10))) // 과거로 설정
                .issuedAt(new Date())
                .claim("type", "access")
                .signWith(jwtTokenProvider.getPrivateKey())
                .compact();

        // then
        assertEquals(TokenValidationResult.EXPIRED, jwtTokenProvider.validateToken(expiredToken));
    }

    @DisplayName("AccessToken과 RefreshToken의 만료시간이 달라야 한다")
    @Test
    void shouldHaveDifferentExpirationForAccessAndRefreshTokens() {

        // given
        String userId = "user123";

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        Date accessExp = Jwts.parser()
                .verifyWith(jwtTokenProvider.getPublicKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getExpiration();

        Date refreshExp = Jwts.parser()
                .verifyWith(jwtTokenProvider.getPublicKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration();

        // then
        assertTrue(refreshExp.after(accessExp));
    }

    @DisplayName("서명이 조작된 토큰 검증 실패")
    @Test
    void shouldFailToValidateTamperedToken() {

        // given: 유효한 토큰 생성 후 일부 조작
        String token = jwtTokenProvider.createAccessToken("hong123");
        String tampered = token.substring(0, token.length() - 2) + "ab";

        // then
        assertEquals(TokenValidationResult.INVALID, jwtTokenProvider.validateToken(tampered));
    }

    @DisplayName("토큰 타입 클레임 누락 시 유효성 검사는 통과해야 하지만 타입 확인이 불가능하다")
    @Test
    void shouldValidateTokenWithoutTypeClaim() {

        // given
        String userId = "user123";
        String tokenWithoutType = Jwts.builder()
                .subject(userId)
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .issuedAt(new Date())
                .signWith(jwtTokenProvider.getPrivateKey())
                .compact();

        // when & then
        assertEquals(TokenValidationResult.VALID, jwtTokenProvider.validateToken(tokenWithoutType));
    }

    @DisplayName("잘못된 문자열을 토큰으로 넘길 경우 INVALID를 반환해야 한다")
    @Test
    void shouldReturnInvalidForMalformedToken() {

        // given
        String malformedToken = "not.a.jwt.token";

        // when & then
        assertEquals(TokenValidationResult.INVALID, jwtTokenProvider.validateToken(malformedToken));
    }

    @DisplayName("토큰에는 tokenId, type, issuedAt이 포함되어야 한다")
    @Test
    void shouldContainTokenIdTypeAndIssuedAtClaims() {

        // given
        String userId = "claimTestUser";

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId);

        Claims claims = Jwts.parser()
                .verifyWith(jwtTokenProvider.getPublicKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        // then
        assertNotNull(claims.get("tokenId"));
        assertEquals("access", claims.get("type"));
        assertNotNull(claims.getIssuedAt());
    }
}
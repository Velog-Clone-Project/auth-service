package com.example.authservice.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    // JWT 키 설정을 담고 있는 프로퍼티 클래스
    private final JwtKeyProperties jwtKeyProperties;

    // Access/Refresh Token 만료 시간(ms)
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // 주 생성자 - 스프링이 자동 주입
    @Autowired
    public JwtTokenProvider(
            JwtKeyProperties jwtKeyProperties,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.jwtKeyProperties = jwtKeyProperties;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 테스트용 생성자
//    public JwtTokenProvider(JwtKeyProperties keyProperties, long accessTokenExp, long refreshTokenExp) {
//        this.jwtKeyProperties = keyProperties;
//        this.accessTokenExpiration = accessTokenExp;
//        this.refreshTokenExpiration = refreshTokenExp;
//    }

    public PrivateKey getPrivateKey() {
        return jwtKeyProperties.getPrivateKey();
    }

    public PublicKey getPublicKey() {
        return jwtKeyProperties.getPublicKey();
    }

    // 주어진 userId와 만료 기간을 기반으로 JWT 토큰 생성
    private String createToken(String userId, long expirationMillis, String type) {

        return Jwts.builder()
                .subject(userId)        // JWT의 주제(Subject) 설정
//                .claim("role", role)  // JWT에 추가적인 클레임 설정 (예: 역할)
                .claim("type", type) // JWT에 토큰 타입 추가 (예: access, refresh)
                .claim("tokenId", UUID.randomUUID().toString()) // JWT에 고유 토큰 ID 추가 (UUID 사용)
                .issuedAt(new Date())   // 발행 시간 설정
                .expiration(Date.from(Instant.now().plusMillis(expirationMillis))) // 만료 시간

                /*
                  JJWT 0.12.x 에서는 암호화 알고리즘을 명시하지 않고, 키 타입에 따라 자동으로 결정되도록 변경되었음
                  key가 HS256용 키이면 자동으로 HMAC-SHA256으로 서명된다.
                  Keys.hmacShaKeyFor(secretKey.getBytes(...)로 생성된 키는 내부적으로 HS256 알고리즘에 적합한 키이다.
                  signWith(key)는 SecretKey 또는 PrivateKey 등의 키 객체만 전달하면 되고
                  알고리즘은 자동 추론되므로 더 이상 SignatureAlgorithm을 명시할 필요가 없다.
                  .signWith(key, SignatureAlgorithm.HS256)
                 */

//                .signWith(secretKey)      // 서명에 사용할 키 설정
                .signWith(getPrivateKey())  // RSA 개인키로 서명
                .compact();         // 최종적으로 JWT 문자열 생성
    }

    public String createAccessToken(String userId) {
        return createToken(userId, accessTokenExpiration, "access");
    }

    public String createRefreshToken(String userId) {
        return createToken(userId, refreshTokenExpiration, "refresh");
    }

    // 토큰 유효성 검사
    public TokenValidationResult validateToken(String token) {
        try {
            // 토큰 파싱 및 서명 검증
            Jwts.parser()
//                    .verifyWith(secretKey)
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token); // 토큰 파싱 시도, 여기서 유효성 + 만료 검증까지

            // 여기까지 도달했다면 유효한 토큰
            return TokenValidationResult.VALID;
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            return TokenValidationResult.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않거나 파싱 오류가 발생한 경우
            return TokenValidationResult.INVALID;
        }
    }

    public String getUserId(String token) {
        return Jwts.parser()
//                .verifyWith(secretKey)
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

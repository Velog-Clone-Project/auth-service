package com.example.authservice.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
// yml 파일에서 jwt로 시작하는 프로퍼티를 읽어와서 필드에 자동으로 매핑
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtKeyProperties {

    // 설정에서 주입받을 PEM 문자열
    private String privateKeyPem;   // 개인키(PKCS8 포맷)의 PEM 문자열
    private String publicKeyPem;    // 공개키(X.509 포맷)의 PEM 문자열

    // JWT 서명에 사용할 대칭키 객체
//    private SecretKey secretKey;

    // JWT 서명에 사용할 비대칭키 객체
    private PrivateKey privateKey;
    private PublicKey publicKey;

    /*
    // 의존성 주입이 끝난 후 실행되는 초기화 메서드
    @PostConstruct
    public void initKey(@Value("${jwt.secret}") String secret) {

        // secretKey를 바이트 배열로 변환하여 HMAC-SHA256 알고리즘에 적합한 SecretKey 객체 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    */

    // 의존성 주입이 완료된 후 자동 실행되는 초기화 메서드
    @PostConstruct
    public void initKeys() {
        try {
            // RSA KeyFactory 생성
            KeyFactory kf = KeyFactory.getInstance("RSA");

            // 개인키 PEM 문자열에서 헤더/푸터/공백 제거
            String privateKeyContent = privateKeyPem.trim()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");     // 공백 제거
            // Base64 디코딩
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            // 개인키 사양으로 객체 생성
            PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(privateKeyBytes);
            // 개인키 객체 생성 및 주입
            this.privateKey = kf.generatePrivate(keySpecPrivate);

            // 공개키 PEM 문자열에서 헤더/푸터/공백 제거
            String publicKeyContent = publicKeyPem.trim()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            // Base64 디코딩
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
            // 공개키 사양으로 객체 생성
            X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(publicKeyBytes);
            // 공개키 객체 생성 및 주입
            this.publicKey = kf.generatePublic(keySpecPublic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }
}

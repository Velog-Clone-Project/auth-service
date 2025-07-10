package com.example.authservice.service;

import com.example.authservice.domain.UserEntity;
import com.example.authservice.domain.UserType;
import com.example.authservice.dto.*;
import com.example.authservice.exception.auth.InvalidCredentialsException;
import com.example.authservice.exception.auth.SocialAccountLoginOnlyException;
import com.example.authservice.exception.token.InvalidRefreshTokenException;
import com.example.authservice.exception.token.TokenExpiredException;
import com.example.authservice.exception.user.*;
import com.example.authservice.jwt.JwtTokenProvider;
import com.example.authservice.jwt.TokenValidationResult;
import com.example.authservice.repository.AuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    // 일반 회원가입
    public SignupResponseDto createUser(SignupRequestDto request) {

        // 1. userId 형식 검사: 영문/숫자 1~16자
        if (!request.getUserId().matches("^[a-zA-Z0-9]{1,16}$")) {
            throw new InvalidUserIdFormatException();
        }
        // 예약어 검사
        List<String> reservedIds = List.of("admin", "root", "system");
        if (reservedIds.contains(request.getUserId().toLowerCase())) {
            throw new ReservedUserIdException();
        }

        // 2. 비밀번호 검사: 최소 8자 + 특수문자 포함
        if (request.getPassword().length() < 8 ||
                !request.getPassword().matches(".*[!@#$%^&*()\\-_=+{};:,<.>].*")) {
            throw new WeakPasswordException();
        }

        // 3. email 중복 체크
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // 4. userId 중복 체크
        if (authRepository.existsByUserId(request.getUserId())) {
            throw new UserIdAlreadyExistsException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // @Builder 패턴을 사용하여 UserEntity 객체 생성
        UserEntity user = UserEntity.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .password(encodedPassword)
                .userType(UserType.GENERAL)
                .build();

        // TODO: 트랜잭션 분리 또는 보상 처리를 고려해야 함 (토큰 저장 실패 시 DB 롤백 불가)
        // save()이후에 에러/예외 발생 시 데이터는 저장됬으나 요청은 실패하기때문에 해결방법이 필요할듯함
        // ex) 만약 이후에 redis에 토큰 저장 중 에러가 발생하면, DB에는 회원 정보가 저장되지만 토큰은 저장되지 않고 요청이 실패하게 된다.
        UserEntity saved = authRepository.save(user);

        // TODO: user-service로 일반 회원가입 이벤트 전송

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        redisService.storeRefreshToken(user.getUserId(), refreshToken);

        return new SignupResponseDto(accessToken, refreshToken);
    }

    // 소셜 회원가입
    public SignupResponseDto createSocialUser(SocialSignupRequestDto request) {

        // 1. userId 형식 검사: 영문/숫자 1~16자
        if (!request.getUserId().matches("^[a-zA-Z0-9]{1,16}$")) {
            throw new InvalidUserIdFormatException();
        }
        // 예약어 검사
        List<String> reservedIds = List.of("admin", "root", "system");
        if (reservedIds.contains(request.getUserId().toLowerCase())) {
            throw new ReservedUserIdException();
        }

        // 2. email 중복 체크
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // 3. userId 중복 체크
        if (authRepository.existsByUserId(request.getUserId())) {
            throw new UserIdAlreadyExistsException();
        }

        UserEntity user = UserEntity.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .userType(request.getUserType())
                .build();

        // TODO: 일반 회원가입과 마찬가지로 트랜잭션 분리 또는 보상 처리를 고려해야 함
        authRepository.save(user);

        // TODO: user-service로 소셜 회원가입 이벤트 전송

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        redisService.storeRefreshToken(request.getUserId(), refreshToken);

        return new SignupResponseDto(accessToken, refreshToken);
    }

    // 일반 로그인
    public LoginResponseDto authenticateUser(LoginRequestDto request) {

        // 가입여부 확인, 이메일로 사용자 조회(없으면 예외 발생)
        UserEntity user = authRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new InvalidCredentialsException()
        );
        // findByEmail()은 Optional로 감싸진 UserEntity를 반환하는데, 여기서는 UserEntity 타입으로 받는 이유는
        // orElseThrow()가 값이 있으면 꺼내고, 없으면 예외를 던지기 때문이다.
        //  즉, UserEneity user = authRepository.findByEmail(email) 은 컴파일 오류를 발생시킨다.

        // 일반 회원만 로그인 허용(소셜 계정은 로그인 불가)
        if (user.getUserType() != UserType.GENERAL) {
            throw new SocialAccountLoginOnlyException();
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        redisService.storeRefreshToken(user.getUserId(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    // 로그아웃
    public void logout(String userId) {

        // 사용자 ID로 사용자 조회(없으면 예외 발생)
        authRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        redisService.deleteRefreshToken(userId);
    }

    // Access/Refresh Token 재발급
    public TokenResponseDto reissueTokens(String refreshToken) {

        // refreshToken 검증
        TokenValidationResult result = jwtTokenProvider.validateToken(refreshToken);
        switch (result) {
            case EXPIRED -> throw new TokenExpiredException();
            case INVALID -> throw new InvalidRefreshTokenException();
        }

        // refreshToken에서 사용자 ID 추출
        String userId = jwtTokenProvider.getUserId(refreshToken);

        // 사용자 ID로 사용자 조회(없으면 예외 발생)
        authRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        // Redis에 저장된 refreshToken과 비교
        if (!redisService.isRefreshTokenValid(userId, refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // Redis에 새로운 refreshToken 저장(기존 토큰은 덮어씌워짐)
        redisService.storeRefreshToken(userId, newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawUser(String userId) {
        // soft delete 방식으로 회원 탈퇴 처리
//       UserEntity user = authRepository.findByUserIdAndDeletedFalse(userId)
//               .orElseThrow(() -> new UserNotFoundException());
//
//       if (user.isDeleted()) {
//           throw new UserAlreadyDeletedException();
//       }
//
//       user.setDeleted(true);
//       // JPA dirty checking을 통해 자동으로 업데이트됨

        // hard delete 방식으로 회원 탈퇴 처리

        // 사용자 ID로 사용자 조회(없으면 예외 발생)
        UserEntity user = authRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        // DB에서 사용자 삭제
        authRepository.delete(user);

        // Redis에서 refreshToken 삭제
        redisService.deleteRefreshToken(userId);
    }
}

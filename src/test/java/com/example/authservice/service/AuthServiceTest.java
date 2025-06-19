package com.example.authservice.service;

import com.example.authservice.domain.UserEntity;
import com.example.authservice.domain.UserType;
import com.example.authservice.dto.*;
import com.example.authservice.exception.auth.InvalidCredentialsException;
import com.example.authservice.exception.auth.SocialAccountLoginOnlyException;
import com.example.authservice.exception.base.BaseCustomException;
import com.example.authservice.exception.token.InvalidRefreshTokenException;
import com.example.authservice.exception.token.TokenExpiredException;
import com.example.authservice.exception.user.*;
import com.example.authservice.jwt.JwtTokenProvider;
import com.example.authservice.jwt.TokenValidationResult;
import com.example.authservice.repository.AuthRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// JUnit5 환경에서 Mockito를 사용할 수 있도록하는 확장(extension) 설정
// JUnit5는 테스트 실행 시 특정 기능을 자동으로 수행할려면 @ExtendWith를 사용해서 확장을 등록해야한다.
// @ExtendWith 설정을 하면 @Mock, @InjectMocks 등의 기능을 자동으로 수행한다.
// Mockito란 Java에서 사용되는 단위 테스트용 mock 프레임워크이다.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // 의존 객체(Dependency)들을 모킹(Mock)하여 테스트 환경을 구성
    // 이후 이 가짜 타입의 객체를 사용해 when().then() 과 같은 방식으로 동작을 설정할 수 있음.
    @Mock
    private AuthRepository authRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    // @Mock으로 만들어진 객체들을 자동으로 주입(Inject)
    // 이렇게하면 테스트 대상 클래스(AuthService)의 실제 인스턴스를 생성하고,
    // 그 안의 의존 객체들(@Mock으로 선언된 객체들)을 자동으로 주입해준다.
    // AuthService는 AuthRepository, RedisService, JwtTokenProvider, PasswordEncoder를 의존하고 있는데
    // 이 의존 객체들을 가짜(@Mock)로 만들어서 주입해야 테스트가 가능하다.
    @InjectMocks
    private AuthService authService;

    //
    @DisplayName("일반회원가입 성공")
    // 테스트 대상 메서드로 인신되어, 테스트 실행 시 자동으로 호출된다.
    @Test
    void signup_withValidGeneralUser_shouldSucceed() {

        // given
        // 회원가입 요청 DTO 객체 생성
        SignupRequestDto request = new SignupRequestDto(
                "Hong123",
                "user@example.com",
                "password@123",
                "홍길동",
                "백엔드를 좋아합니다");

        // Mockito의 when() 메서드는 특정 상황에서 어떤 동작이 일어날지를 "설정" 하는 메서드.
        // .thenReturn() 메서드는 해당 상황에서 어떤 값을 반환할지를 지정한다.
        // mock 객체가 특정 메서드 호출 시 어떤 값을 반환할지 설정
        // 비밀번호 인코딩 설정, "password@123"을 인코딩했을 때 "encodedPassword"를 반환하도록 설정
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        UserEntity user = UserEntity.builder().userId(request.getUserId()).email(request.getEmail()).password(encodedPassword).userType(UserType.GENERAL).build();

        // authRepository.save() 메서드로 UserEntity 타입의 데이터를 저장했을때
        // 위에서 생성한 user 객체를 반환하도록 설정
        // any(UserEntity.class)는 Mockito의 메서드로, 어떤 UserEntity 객체가 오더라도 상관없이 매칭되도록 설정
        // Mockito는 기본적으로 객체의 동일성 또는 equals()로 비교한다.
        // 테스트 코드 안에서 생성한 user 객체와, authService.createUser() 내부에서 생성해서 반환한 UserEntity는 다른 인스턴스이다.
        // 즉, authRepository.save(user) 라고 지정하면 내부에서 새로 생성된 UserEntity는 서로 다른 인스턴스이므로 매칭되지 않는다.
        // 아무리 속성값이 같아도, new UserEntity()로 새로 만들었으면 다른 객체로 인식된다.
        // even equals()를 오버라이드했더라도, Mockito의 default matching 전략은 reference equality 또는 명시적 매처 사용이다.
        // 따라서 유연하게 매칭하려면 any(UserEntity.class)를 사용해야 테스트가 성공한다.
        when(authRepository.save(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user.getUserId())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user.getUserId())).thenReturn("refresh-token");

        // when
        // 실제 서비스 로직 호출 (회원가입 실행)
        SignupResponseDto response = authService.createUser(request);

        // then
        // 응답으로 받은 토큰이 설정한 값과 일치하는지 검증
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        // verity() 메서드는 특정 메서드가 호출되었는지 검증하는 메서드
        // 여기서는 redisService의 storeRefreshToken() 메서드가 호출되었는지 검증
        // verify()는 어떤 mock 객체를 검증할지 인자로 받아 해당 객체(redisService)가 호출되었는지를 추적한다.
        // 그 뒤의 .storeRefreshToken()은 실제로 이 mock 객체에서 호출되었어야 할 메서드 호출 기록과 비교한다.

        // 1. Mockito는 redisService라는 mock 객체의 모든 메서드 호출 기록을 보관하고 있다.
        // 2. verify(redisService)로 시작하면 redisService 객체의 메서드 호출 기록을 검증할 준비를 한다.
        // 3. 이어서 .storeRefreshToken(...) 라고 체이닝하면 해당 메서드 호출이 진짜 있었는지 기록에서 찾는다.
        verify(redisService, times(1))
                .storeRefreshToken(user.getUserId(), response.getRefreshToken());

        verify(authRepository, times(1))
                .save(ArgumentMatchers.any(UserEntity.class));

        // 단순히 메서드 호출 여부만 확인하는 것이 아니라, 호출된 인자의 값까지 정확히 일치해야 한다.
        // 예를 들어 storeRefreshToken("otherUser", "otherToken")이 호출되었으면 테스트는 실패한다.
        // 즉, verify(redisService).storeRefreshToken("Hong123", "refresh-token")처럼 정확한 호출을 해야 테스트가 통과된다.
    }

    @DisplayName("회원가입 실패 - userId 형식 오류 예외")
    @Test
    void signup_withInvalidUserId_shouldThrowException() {

        // given
        SignupRequestDto request = new SignupRequestDto(
                "invalid!!",
                "user@example.com",
                "pass@123",
                "홍길동",
                "백엔드를 좋아합니다");

        // when
        // assertThrows() 메서드는 특정 코드 블록이 예외를 던지는지 검증한다.
        // 첫번째 인자는 예상되는 예외를 전달함으로써 createUser(request) 메서드를 실행했을때
        // InvalidUserIdFormatException이 발생해야만 테스트가 통과된다.
        // 두번째 인자는 예외가 발생할 것으로 예상되는 테스트 대상 코드를 람다식으로 전달한다.
        // 예외가 발생하면 BaseCustomException 타입의 ex 변수에 할당된다.(추후 상태코드 검증을 위해 사용)
        BaseCustomException ex = assertThrows(InvalidUserIdFormatException.class, () -> authService.createUser(request));

        // then
        // 예외가 발생했을 때, 상태 코드가 400인지 검증
        // assertEquals(expected, actual) 구조로 되어 있으므로, 400이 우리가 기대하는 상태코드이고,
        // ex.getStatusCode()가 실제로 반환된 상태코드이다.
        assertEquals(400, ex.getStatusCode());
    }

    @DisplayName("일반 회원가입 - userId 예약어 예외")
    @Test
    void general_signup_reserved_user_id() {

        // given
        SignupRequestDto request = new SignupRequestDto(
                "admin",
                "user@example.com",
                "password@123",
                "홍길동",
                "소개");

        // when
        BaseCustomException ex = assertThrows(ReservedUserIdException.class, () -> authService.createUser(request));

        // then
        assertEquals(400, ex.getStatusCode());
    }

    @DisplayName("회원가입 실패 - password 형식 오류 예외")
    @Test
    void signup_withWeakPassword_shouldThrowException() {

        // given
        SignupRequestDto request = new SignupRequestDto(
                "validID",
                "user@example.com",
                "weakpass",
                "홍길동",
                "백엔드를 좋아합니다");

        // when
        BaseCustomException ex = assertThrows(WeakPasswordException.class, () -> authService.createUser(request));

        // then
        assertEquals(400, ex.getStatusCode());
    }

    @DisplayName("회원가입 실패 - 이메일 중복 예외")
    @Test
    void signup_withDuplicatedEmail_shouldThrowException() {

        // given
        SignupRequestDto request = new SignupRequestDto(
                "validId",
                "user@example.com",
                "password@123",
                "홍길동",
                "백엔드를 좋아합니다");

        when(authRepository.existsByEmail("user@example.com")).thenReturn(true);

        // when
        BaseCustomException ex = assertThrows(EmailAlreadyExistsException.class, () -> authService.createUser(request));

        // then
        assertEquals(409, ex.getStatusCode());
    }


    @DisplayName("회원가입 실패 - userId 중복 예외")
    @Test
    void signup_withDuplicatedUserId_shouldThrowException() {

        // given
        SignupRequestDto request = new SignupRequestDto(
                "duplicateId",
                "user@example.com",
                "password@123",
                "홍길동",
                "백엔드를 좋아합니다");

        when(authRepository.existsByUserId("duplicateId")).thenReturn(true);

        // when
        BaseCustomException ex = assertThrows(UserIdAlreadyExistsException.class, () -> authService.createUser(request));

        // then
        assertEquals(409, ex.getStatusCode());
    }


    @DisplayName("소셜회원가입 성공")
    @Test
    void socialSignup_withValidInfo_shouldSucceed() {

        // given
        SocialSignupRequestDto request = new SocialSignupRequestDto(
                "hong123",
                "user@example.com",
                UserType.KAKAO,
                "홍길동",
                "백엔드를 좋아합니다");

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .userId(request.getUserId())
                .userType(request.getUserType())
                .build();

        when(authRepository.save(ArgumentMatchers.any(UserEntity.class))).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user.getUserId())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user.getUserId())).thenReturn("refresh-token");

        // when
        SignupResponseDto response = authService.createSocialUser(request);

        // then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(redisService, times(1))
                .storeRefreshToken(user.getUserId(), "refresh-token");
        verify(authRepository, times(1))
                .save(ArgumentMatchers.any(UserEntity.class));
    }

    @DisplayName("소셜회원가입 실패 - 이메일 중복 예외")
    @Test
    void socialSignup_withDuplicatedEmail_shouldThrowException() {

        // given
        SocialSignupRequestDto request = new SocialSignupRequestDto(
                "hong123",
                "user@example.com",
                UserType.KAKAO,
                "홍길동",
                "소개");

        when(authRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // then
        assertThrows(EmailAlreadyExistsException.class, () -> authService.createSocialUser(request));
    }

    @DisplayName("소셜회원가입 실패 - userId 중복 예외")
    @Test
    void socialSignup_withDuplicatedUserId_shouldThrowException() {

        // given
        SocialSignupRequestDto request = new SocialSignupRequestDto(
                "hong123",
                "user@example.com",
                UserType.KAKAO,
                "홍길동",
                "소개");

        when(authRepository.existsByUserId(request.getUserId())).thenReturn(true);

        // then
        assertThrows(UserIdAlreadyExistsException.class, () -> authService.createSocialUser(request));
    }

    @DisplayName("소셜 회원가입 - 예약어 userId 예외")
    @Test
    void social_signup_reserved_user_id() {

        // given
        SocialSignupRequestDto request = new SocialSignupRequestDto(
                "admin",
                "user@example.com",
                UserType.KAKAO,
                "홍길동",
                "소개");

        // when & then
        assertThrows(ReservedUserIdException.class, () -> authService.createSocialUser(request));
    }

    @DisplayName("로그인 성공")
    @Test
    void login_withValidCredentials_shouldSucceed() {

        // given
        LoginRequestDto request = new LoginRequestDto("user@example.com", "password123");

        UserEntity user = UserEntity.builder()
                .email("user@example.com")
                .userId("hong123")
                .password("encodedPassword")
                .userType(UserType.GENERAL)
                .build();

        // Optional.of(user)는 Optional 객체를 생성하여 user가 존재함을 나타낸다.
        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(user.getUserId())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(user.getUserId())).thenReturn("refresh-token");

        // when
        LoginResponseDto response = authService.authenticateUser(request);

        // then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(redisService, times(1))
                .storeRefreshToken(user.getUserId(), "refresh-token");
    }

    @DisplayName("로그인 실패 - 비밀번호 불일치 예외")
    @Test
    void login_withIncorrectPassword_shouldThrowException() {

        // given
        LoginRequestDto request = new LoginRequestDto("user@example.com", "wrong");

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .userId("hong123")
                .password("encodedPassword")
                .userType(UserType.GENERAL)
                .build();

        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        // when & then
        assertThrows(InvalidCredentialsException.class, () -> authService.authenticateUser(request));
    }

    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    @Test
    void login_fail_with_nonexistent_email() {

        // given
        LoginRequestDto request = new LoginRequestDto("unknown@example.com", "password");

        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidCredentialsException.class, () -> authService.authenticateUser(request));
    }

    @DisplayName("로그인 실패 - 소셜 계정 로그인 차단")
    @Test
    void login_fail_with_social_account() {
        // given
        LoginRequestDto request = new LoginRequestDto("social@example.com", "password");

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .userId("social123")
                .password("encodedPassword")
                .userType(UserType.KAKAO) // 소셜 계정
                .build();

        when(authRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // when & then
        assertThrows(SocialAccountLoginOnlyException.class, () -> authService.authenticateUser(request));
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_withValidUserId_shouldSucceed() {

        // given
        String userId = "hong123";

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .email("hong@example.com")
                .userType(UserType.GENERAL)
                .build();

        when(authRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // when
        authService.logout(userId);

        // then
        verify(redisService).deleteRefreshToken(userId);
    }

    @DisplayName("토큰 재발급 성공")
    @Test
    void reissue_withValidRefreshToken_shouldSucceed() {

        // given
        String oldRefreshToken = "valid-refresh-token";
        String userId = "hong123";

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .email("hong@example.com")
                .userType(UserType.GENERAL)
                .build();

        when(authRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserId(oldRefreshToken)).thenReturn(userId);

        when(redisService.isRefreshTokenValid(userId, oldRefreshToken)).thenReturn(true);

        when(jwtTokenProvider.createAccessToken(userId)).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken(userId)).thenReturn("new-refresh-token");

        // when
        TokenResponseDto response = authService.reissueTokens(oldRefreshToken);

        // then
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(redisService).storeRefreshToken(userId, "new-refresh-token");
    }

    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰 예외")
    @Test
    void reissue_withInvalidToken_shouldThrowException() {

        // given
        String invalidRefreshToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(TokenValidationResult.INVALID);

        // when & then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.reissueTokens(invalidRefreshToken));
    }

    @DisplayName("토큰 재발급 실패 - Redis와 불일치 예외")
    @Test
    void reissue_whenRedisTokenMismatch_shouldThrowException() {

        // given
        String refreshToken = "valid-refresh-token";
        String userId = "hong123";

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .email("hong@example.com")
                .userType(UserType.GENERAL)
                .build();

        when(authRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserId(refreshToken)).thenReturn(userId);
        when(redisService.isRefreshTokenValid(userId, refreshToken)).thenReturn(false);

        // when & then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.reissueTokens(refreshToken));
    }

    @DisplayName("토큰 재발급 실패 - 만료된 토큰")
    @Test
    void token_reissue_with_expired_token() {

        // given
        String expiredToken = "expired-refresh-token";

        when(jwtTokenProvider.validateToken(expiredToken)).thenReturn(TokenValidationResult.EXPIRED);

        // when & then
        assertThrows(TokenExpiredException.class, () -> authService.reissueTokens(expiredToken));
    }

    @DisplayName("회원탈퇴 성공")
    @Test
    void withdraw_withValidUser_shouldSucceed() {

        // given
        UserEntity user = UserEntity.builder()
                .email("user@example.com")
                .userId("hong123")
                .password("encodedPassword")
                .userType(UserType.GENERAL)
                .build();

        String userId = user.getUserId();

        when(authRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // when
        authService.withdrawUser(userId);

        // then
        verify(authRepository).findByUserId(userId); // 사용자 조회 호출 확인
        verify(authRepository).delete(user); // 사용자 삭제 호출 확인
        verify(redisService).deleteRefreshToken(userId); // Redis 토큰 삭제 호출 확인
    }

    @DisplayName("회원탈퇴 실패 - 존재하지 않는 유저")
    @Test
    void withdraw_withMissingUser_shouldThrowException() {

        // given
        String userId = "missing-user";
        when(authRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> authService.withdrawUser(userId));
    }
}
package com.example.authservice.service;

import com.example.authservice.domain.UserEntity;
import com.example.authservice.exception.user.UserNotFoundException;
import com.example.authservice.repository.AuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalAuthService {

    private final AuthRepository authRepository;
    private final RedisService redisService;

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String userId) {
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
                .orElseThrow(UserNotFoundException::new);

        // DB에서 사용자 삭제
        authRepository.delete(user);

        // Redis에서 refreshToken 삭제
        redisService.deleteRefreshToken(userId);
    }
}

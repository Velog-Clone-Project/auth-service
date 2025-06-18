package com.example.authservice.repository;

import com.example.authservice.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// User 엔티티에 대한 DB 접근을 담당하는 리포지토리 인터페이스
// JpaRepository를 상속받아 기본적인 CRUD 기능 자동 제공
// JpaRepository<Entity Type, PK Type> 형식으로 사용
public interface AuthRepository extends JpaRepository<UserEntity, Long> {

    // 이메일로 사용자 조회(Optional로 null 처리 대응)
    Optional<UserEntity> findByEmail(String email);

    // 사용자 ID로 사용자 조회(userId는 닉네임이 아님, 서비스 고유 ID)
    Optional<UserEntity> findByUserId(String userId);

    // 해당 이메일을 가진 사용자가 존재하는지 여부 반환
    boolean existsByEmail(String email);

    // 해당 userId를 가진 사용자가 존재하는지 여부 반환
    boolean existsByUserId(String userId);

//    Optional<UserEntity> findByUserIdAndDeletedFalse(String userId);
}

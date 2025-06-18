package com.example.authservice.domain;

import jakarta.persistence.*;
import lombok.*;

// JPA 엔티티로 매핑(DB 테이블과 연결)
@Entity

// 테이블 이름을 auth_user로 설정
@Table(name = "auth_user")

// Lombok 어노테이션을 사용하여 getter, setter 매서드 자동으로 생성
@Getter
@Setter

// 기본 생성자 생성
// JPA에서 필수
// 외부코드가 실수로 new UserEntity()로 객체 생성하지 못하도록 protected로 접근 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// 모든 필드를 포함하는 생성자 생성
// 테스트코드 등 다른곳에서 사용가능성 있음
@AllArgsConstructor

// 빌더 패턴 적용, User.builder() 형태로 객체 생성 가능
@Builder
public class UserEntity {

    // 기본 키(PK) 필드 지정
    @Id
    // auto-increment 방식으로 PK 값 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 고유 식별자 (서비스 전역에 사용)
    // null 불가, 중복 불가 제약 조건
    @Column(nullable = false, unique = true)
    private String userId;

    // 로그인 이메일 (소셜 / 일반 공통)
    // null 불가, 중복 불가 제약 조건
    @Column(nullable = false, unique = true)
    private String email;

    // 일반 로그인일 경우에만 필요, 소셜 유저는 null
    // 명시적으로 지정, 기본적으로 nullable = true
    @Column()
    private String password;

    // 일반 회원인지 소셜 회원인지 구분
    // Enum을 문자열로 저장 (ex: "GENERAL")
    @Enumerated(EnumType.STRING)
    // null 불가 제약 조건
    @Column(nullable = false)
    private UserType userType;

    // 회원 탈퇴 여부 - soft delete
//    @Builder.Default
//    @Column(nullable = false)
//    private boolean deleted = false;
}

package com.example.authservice.dto;

import com.example.authservice.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialSignupRequestDto {

    // 문자열이고, null 또는 공백/빈 문자열까지 검사
    // "", " " 등은 허용하지 않음
    @NotBlank
    private String userId;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    // null 여부만 검사
    // "", " " 등은 허용
    @NotNull
    private UserType userType;

    // user-service에서 사용될 필드
    private String profileName;
    private String bio;
}

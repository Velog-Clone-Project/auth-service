package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {

    private String accessToken;

    @JsonIgnore // JSON으로 직렬화할 때 이 필드는 제외됨
    private String refreshToken;
}

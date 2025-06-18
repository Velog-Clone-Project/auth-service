package com.example.authservice.dto;

public interface OAuth2Response {

    String getProvider();
    String getEmail();
    String getUserId();
    String getProfileName();
    String getProfileImageUrl();
}

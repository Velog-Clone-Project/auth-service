package com.example.authservice.exception.cookie;

import com.example.common.exception.BaseCustomException;

public class RefreshTokenNotFoundException extends BaseCustomException {

    public RefreshTokenNotFoundException() {
        super("Refresh token not found in cookie");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
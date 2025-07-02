package com.example.authservice.exception.token;

import com.example.common.exception.BaseCustomException;

public class InvalidRefreshTokenException extends BaseCustomException {

    public InvalidRefreshTokenException() {
        super("Invalid refresh token");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
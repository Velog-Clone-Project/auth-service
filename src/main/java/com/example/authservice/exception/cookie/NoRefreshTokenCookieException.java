package com.example.authservice.exception.cookie;

import com.example.common.exception.BaseCustomException;

public class NoRefreshTokenCookieException extends BaseCustomException {

    public NoRefreshTokenCookieException() {
        super("No refresh token cookie found");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
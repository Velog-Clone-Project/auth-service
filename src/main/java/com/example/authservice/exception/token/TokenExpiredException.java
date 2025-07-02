package com.example.authservice.exception.token;

import com.example.common.exception.BaseCustomException;

public class TokenExpiredException extends BaseCustomException {

    public TokenExpiredException() {
        super("Token has expired");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
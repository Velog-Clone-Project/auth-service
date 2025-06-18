package com.example.authservice.exception.token;

import com.example.authservice.exception.base.BaseCustomException;

public class TokenExpiredException extends BaseCustomException {

    public TokenExpiredException() {
        super("Token has expired");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
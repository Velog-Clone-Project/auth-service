package com.example.authservice.exception.auth;

import com.example.common.exception.BaseCustomException;

public class InvalidCredentialsException extends BaseCustomException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
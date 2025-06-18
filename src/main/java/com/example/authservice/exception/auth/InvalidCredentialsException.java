package com.example.authservice.exception.auth;

import com.example.authservice.exception.base.BaseCustomException;

public class InvalidCredentialsException extends BaseCustomException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
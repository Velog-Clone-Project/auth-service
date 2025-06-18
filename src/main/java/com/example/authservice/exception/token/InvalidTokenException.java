package com.example.authservice.exception.token;

import com.example.authservice.exception.base.BaseCustomException;

public class InvalidTokenException extends BaseCustomException {

    public InvalidTokenException() {
        super("Invalid token");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
package com.example.authservice.exception.token;

import com.example.authservice.exception.base.BaseCustomException;

public class InvalidAccessTokenException extends BaseCustomException {

    public InvalidAccessTokenException() {
        super("Invalid access token");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
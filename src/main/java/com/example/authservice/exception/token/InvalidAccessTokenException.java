package com.example.authservice.exception.token;

import com.example.common.exception.BaseCustomException;

public class InvalidAccessTokenException extends BaseCustomException {

    public InvalidAccessTokenException() {
        super("Invalid access token");
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
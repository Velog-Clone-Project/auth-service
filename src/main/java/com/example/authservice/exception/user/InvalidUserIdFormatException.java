package com.example.authservice.exception.user;

import com.example.authservice.exception.base.BaseCustomException;

public class InvalidUserIdFormatException extends BaseCustomException {

    public InvalidUserIdFormatException() {
        super("User ID must be alphanumeric and up to 16 characters");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
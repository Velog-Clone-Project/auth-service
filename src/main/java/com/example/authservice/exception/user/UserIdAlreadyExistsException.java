package com.example.authservice.exception.user;

import com.example.authservice.exception.base.BaseCustomException;

public class UserIdAlreadyExistsException extends BaseCustomException {

    public UserIdAlreadyExistsException() {
        super("User ID is already in use");
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
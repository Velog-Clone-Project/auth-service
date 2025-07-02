package com.example.authservice.exception.user;

import com.example.common.exception.BaseCustomException;

public class UserIdAlreadyExistsException extends BaseCustomException {

    public UserIdAlreadyExistsException() {
        super("User ID is already in use");
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
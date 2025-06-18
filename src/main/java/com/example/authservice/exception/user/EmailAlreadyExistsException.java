package com.example.authservice.exception.user;

import com.example.authservice.exception.base.BaseCustomException;

public class EmailAlreadyExistsException extends BaseCustomException {

    public EmailAlreadyExistsException() {
        super("Email is already registered");
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
package com.example.authservice.exception.user;

import com.example.authservice.exception.base.BaseCustomException;

public class WeakPasswordException extends BaseCustomException {

    public WeakPasswordException() {
        super("Password must be at least 8 characters and contain a special character");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
package com.example.authservice.exception.auth;

import com.example.authservice.exception.base.BaseCustomException;

public class GeneralAccountLoginOnlyException extends BaseCustomException {

    public GeneralAccountLoginOnlyException() {

        super("This email is registered as a general login account");
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
package com.example.authservice.exception.auth;

import com.example.common.exception.BaseCustomException;

public class SocialAccountLoginOnlyException extends BaseCustomException {

    public SocialAccountLoginOnlyException() {
        super("This email is registered as a social login account");
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
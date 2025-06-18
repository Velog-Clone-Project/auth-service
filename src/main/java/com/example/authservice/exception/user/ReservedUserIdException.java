package com.example.authservice.exception.user;

import com.example.authservice.exception.base.BaseCustomException;

public class ReservedUserIdException extends BaseCustomException {

    public ReservedUserIdException() {
        super("This user ID is not allowed");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
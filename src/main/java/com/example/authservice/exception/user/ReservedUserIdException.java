package com.example.authservice.exception.user;

import com.example.common.exception.BaseCustomException;

public class ReservedUserIdException extends BaseCustomException {

    public ReservedUserIdException() {
        super("This user ID is not allowed");
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
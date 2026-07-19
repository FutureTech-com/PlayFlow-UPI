package com.payflow.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class UnauthorizedException extends PayflowException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

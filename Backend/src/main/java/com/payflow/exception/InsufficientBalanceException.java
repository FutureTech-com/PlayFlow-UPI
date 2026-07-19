package com.payflow.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class InsufficientBalanceException extends PayflowException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

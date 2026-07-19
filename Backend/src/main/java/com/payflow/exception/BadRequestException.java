package com.payflow.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class BadRequestException extends PayflowException {
	public BadRequestException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}
}

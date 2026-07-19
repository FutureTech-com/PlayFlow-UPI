package com.payflow.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class PayflowException extends RuntimeException {
	private final HttpStatus status;
	
	protected PayflowException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}

package com.rocket.devops.rdoi.common.exception;

public class RDORuntimeException extends RuntimeException {
	public RDORuntimeException(String message) {
		super(message);
	}
	public RDORuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	public RDORuntimeException(Throwable cause) {
		super(cause);
	}
}

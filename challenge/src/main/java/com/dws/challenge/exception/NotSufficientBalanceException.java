package com.dws.challenge.exception;

// This class is meant for handling the customized exception for Insufficient Balance

public class NotSufficientBalanceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotSufficientBalanceException(String message) {
		super(message);
	}
}
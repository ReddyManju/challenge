package com.dws.challenge.exception;

// This class is meant for handling the customized exception for transferring amount
public class AmountTransferPojoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AmountTransferPojoException(String message) {
		super(message);
	}

}

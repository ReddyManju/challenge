package com.dws.challenge.exception;

//This class is meant for handling the customized exception for same amount needs to be transfered 

public class TransferSameAccountException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransferSameAccountException(String message) {
		super(message);
	}
}
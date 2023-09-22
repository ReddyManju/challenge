package com.dws.challenge.transaction;

@FunctionalInterface
public interface TransactionCallbackMechanism {

	public void process();
}

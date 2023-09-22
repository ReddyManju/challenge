package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.AmountTransferPojoException;
import com.dws.challenge.exception.NotSufficientBalanceException;
import com.dws.challenge.exception.TransferSameAccountException;

@Service
public class TransferValidator {
	void validate(final Account accountFrom, final Account accountTo, final BigDecimal amount)
			throws AccountNotFoundException, NotSufficientBalanceException {

		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new AmountTransferPojoException("Transfer amount not valid. Please try with valid amount");
		}

		if (accountFrom == null) {
			throw new AccountNotFoundException("Account " + accountFrom + "not found");
		}

		if (accountTo == null) {
			throw new AccountNotFoundException("Account " + accountTo + "not found");
		}

		if (sameAccount(accountFrom, accountTo)) {
			throw new TransferSameAccountException("Self Transfer not allowed");
		}

		if (!enoughBalance(accountFrom, amount)) {
			throw new NotSufficientBalanceException("Insufficient balance in account");
		}
	}

	private boolean sameAccount(final Account accountFrom, final Account accountTo) {
		return accountFrom.getAccountId().equals(accountTo.getAccountId());
	}

	private boolean enoughBalance(final Account account, final BigDecimal amount) {
		return account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
	}
}
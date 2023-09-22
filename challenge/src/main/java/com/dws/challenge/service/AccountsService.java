package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.AmountTransferPojoException;
import com.dws.challenge.exception.NotSufficientBalanceException;
import com.dws.challenge.exception.TransferSameAccountException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.transaction.AccountTransactionManager;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	public AccountTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(AccountTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public TransferValidator getTransferValidator() {
		return transferValidator;
	}

	public void setTransferValidator(TransferValidator transferValidator) {
		this.transferValidator = transferValidator;
	}

	public AccountsRepository getAccountsRepository() {
		return accountsRepository;
	}

	private AccountTransactionManager transactionManager;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private TransferValidator transferValidator;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
		this.transactionManager = new AccountTransactionManager(accountsRepository);
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void amountTransfer(final String fromAccount, final String toAccount, final BigDecimal transferAmount)
			throws AmountTransferPojoException, AccountNotFoundException, TransferSameAccountException,
			NotSufficientBalanceException {

		// Validating the accounts Ids and balance amount

		transferValidator.validate(getAccount(fromAccount), getAccount(toAccount), transferAmount);
		transactionManager.doInTransaction(() -> {

			this.debit(fromAccount, transferAmount);
			this.credit(toAccount, transferAmount);
		});

		transactionManager.commit();

		notificationService.notifyAboutTransfer(getAccount(fromAccount), "The transfer to the account with ID "
				+ toAccount + " is now complete for the amount of " + transferAmount + ".");
		notificationService.notifyAboutTransfer(getAccount(toAccount),
				"The account with ID + " + fromAccount + " has transferred " + transferAmount + " into your account.");
	}

	// This method is meant for Debit the amount from Source Account
	private Account debit(String accountId, BigDecimal amount) throws AmountTransferPojoException {

		final Account account = transactionManager.getRepoProxy().getAccount(accountId);
		if (account == null) {
			throw new AmountTransferPojoException("Account does not exist");
		}
		if (account.getBalance().compareTo(amount) == -1) {
			throw new AmountTransferPojoException("Insufficient balance in account");
		}
		BigDecimal bal = account.getBalance().subtract(amount);
		account.setBalance(bal);
		return account;
	}

	// This method is meant for Credit the amount from Destination Account
	private Account credit(String accountId, BigDecimal amount) throws AmountTransferPojoException {

		final Account account = transactionManager.getRepoProxy().getAccount(accountId);
		if (account == null) {
			throw new AmountTransferPojoException("Account does not exist");
		}
		BigDecimal bal = account.getBalance().add(amount);
		account.setBalance(bal);
		return account;
	}
}

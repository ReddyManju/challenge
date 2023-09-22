package com.dws.challenge.transaction;

import java.lang.reflect.Proxy;
import java.util.Map;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

// This class defines for  Transaction Management  Operations  

public class AccountTransactionManager {

	private final AccountsRepository accountsRepository;

	private TransacrionInvocationHandler<Account> handler;

	@Getter
	private boolean autoCommit = false;

	public TransacrionInvocationHandler<Account> getHandler() {
		return handler;
	}

	public void setHandler(TransacrionInvocationHandler<Account> handler) {
		this.handler = handler;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public AccountsRepository getRepoProxy() {
		return repoProxy;
	}

	public void setRepoProxy(AccountsRepository repoProxy) {
		this.repoProxy = repoProxy;
	}

	public AccountsRepository getAccountsRepository() {
		return accountsRepository;
	}

	@Getter
	private AccountsRepository repoProxy;

	public AccountTransactionManager(AccountsRepository repository) {
		this.accountsRepository = repository;

		handler = new TransacrionInvocationHandler<Account>(accountsRepository);
		repoProxy = (AccountsRepository) Proxy.newProxyInstance(AccountsRepository.class.getClassLoader(),
				new Class[] { AccountsRepository.class }, handler);

	}

	public void doInTransaction(TransactionCallbackMechanism callback) {
		TransactionContextObject<Account, Account> context = new TransactionContextObject<>();
		ThreadLocal<TransactionContextObject<Account, Account>> localContext = handler.getLocalContext();
		localContext.set(context);
		try {
			callback.process();
			if (autoCommit) {
				commit();
			}
		} catch (Exception e) {
			rollBack();
			throw e;
		} finally {

		}

	}

	public void commit() {
		TransactionContextObject<Account, Account> localContext = handler.getLocalContext().get();
		Map<Account, Account> savePoints = localContext.getSavePoints();

		savePoints.entrySet().forEach(entry -> {
			Account key = entry.getKey();
			Account value = entry.getValue();
			value.setBalance(key.getBalance());
		});
	}

	public void rollBack() {

		TransactionContextObject<Account, Account> localContext = handler.getLocalContext().get();
		localContext.getSavePoints().clear();
	}

}

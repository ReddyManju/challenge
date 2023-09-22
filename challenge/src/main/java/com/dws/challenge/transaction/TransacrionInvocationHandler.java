package com.dws.challenge.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

public class TransacrionInvocationHandler<E> implements InvocationHandler {

	private final AccountsRepository accountsRepository;

	@Getter
	ThreadLocal<TransactionContextObject<Account, Account>> localContext = new ThreadLocal<>();;

	public ThreadLocal<TransactionContextObject<Account, Account>> getLocalContext() {
		return localContext;
	}

	public void setLocalContext(ThreadLocal<TransactionContextObject<Account, Account>> localContext) {
		this.localContext = localContext;
	}

	TransacrionInvocationHandler(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		String methodName = method.getName();
		if (methodName.startsWith("get")) {
			Account account = accountsRepository.getAccount((String) args[0]);
			BigDecimal balanceCopy = BigDecimal.ZERO;
			Account proxyAccount = new Account(account.getAccountId(), balanceCopy.add(account.getBalance()));

			TransactionContextObject<Account, Account> context = localContext.get();
			if (context != null) {
				context.getSavePoints().put(proxyAccount, account);
				return proxyAccount;
			} else {

				return account;
			}
		}

		return null;
	}
}
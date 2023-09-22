package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.NotSufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private NotificationService notificationService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void amountTransfer_TransactionCommit() throws Exception {
		Account accountFrom = new Account("Id-341");
		accountFrom.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(accountFrom);
		Account accountTo = new Account("Id-342");
		accountTo.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(accountTo);
		this.accountsService.amountTransfer("Id-341", "Id-342", new BigDecimal(1000));
		assertThat(this.accountsService.getAccount("Id-341").getBalance()).isEqualTo(BigDecimal.ZERO);
		assertThat(this.accountsService.getAccount("Id-342").getBalance()).isEqualTo(new BigDecimal(2000));

	}

	@Test
	public void amountTransfer_TransactionRollBack() throws Exception {
		Account accountFrom = new Account("Id-350");
		accountFrom.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(accountFrom);
		Account accountTo = new Account("Id-351");
		accountTo.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(accountTo);
		this.accountsService.amountTransfer("Id-350", "Id-351", new BigDecimal(1000));

		try {
			// make transfer when balance insufficient
			this.accountsService.amountTransfer("Id-350", "Id-351", new BigDecimal(500));
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Insufficient balance in account");
		}
		// Transaction will be rollBack and no account will be updated
		assertThat(this.accountsService.getAccount("Id-350").getBalance()).isEqualTo(BigDecimal.ZERO);
		assertThat(this.accountsService.getAccount("Id-351").getBalance()).isEqualTo(new BigDecimal(2000));

	}

	@Test
	public void amountTransfer_TransactionRollBackOnNonExistingAccount() throws Exception {
		// make transfer To an Account which do not exist
		Account accountFrom = new Account("Id-360");
		accountFrom.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(accountFrom);
		try {
			this.accountsService.amountTransfer("Id-360", "Id-361", new BigDecimal(500));
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("Account does not exist");
		}
		// Transaction will be rollBack and no debit will happen
		assertThat(this.accountsService.getAccount("Id-360").getBalance()).isEqualTo(new BigDecimal(1000));

	}

	@Test
	public void amountTransfer_should_fail_when_accountNotEnoughFunds() {
		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		this.accountsService.createAccount(new Account(accountFromId));
		this.accountsService.createAccount(new Account(accountToId));
		try {
			this.accountsService.amountTransfer(accountFromId, accountToId, new BigDecimal(100));
			fail("Should have failed because account does not have enough balance for the transfer");
		} catch (NotSufficientBalanceException nbe) {
			assertThat(nbe.getMessage()).isEqualTo("Insufficient balance in account");
		}
	}

	@Test
	public void amountTransfer_should_transferFunds() {
		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		final Account accountFrom = new Account(accountFromId, new BigDecimal("500.99"));
		final Account accountTo = new Account(accountToId, new BigDecimal("20.00"));
		final BigDecimal transferAmount = new BigDecimal("200.99");

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);

		this.accountsService.amountTransfer(accountFromId, accountToId, transferAmount);

		assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("300.00"));
		assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("220.99"));

		verifyNotifications(accountFrom, accountTo, transferAmount);
	}

	@Test
	public void amountTransfer_should_transferFunds_when_balanceJustEnough() {

		final String accountFromId = UUID.randomUUID().toString();
		final String accountToId = UUID.randomUUID().toString();
		final Account accountFrom = new Account(accountFromId, new BigDecimal("100.01"));
		final Account accountTo = new Account(accountToId, new BigDecimal("20.00"));
		final BigDecimal transferAmount = new BigDecimal("100.01");

		this.accountsService.createAccount(accountFrom);
		this.accountsService.createAccount(accountTo);

		this.accountsService.amountTransfer(accountFromId, accountToId, transferAmount);

		assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("0.00"));
		assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("120.01"));

		verifyNotifications(accountFrom, accountTo, transferAmount);
	}

	private void verifyNotifications(final Account accountFrom, final Account accountTo, final BigDecimal amount) {
		verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountFrom,
				"The transfer to the account with ID " + accountTo.getAccountId()
						+ " is now complete for the amount of " + amount + ".");
		verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountTo, "The account with ID + "
				+ accountFrom.getAccountId() + " has transferred " + amount + " into your account.");
	}
}
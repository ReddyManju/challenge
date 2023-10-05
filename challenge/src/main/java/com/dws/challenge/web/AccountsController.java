package com.dws.challenge.web;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.AmountTransferPojo;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.AmountTransferPojoException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.NotSufficientBalanceException;
import com.dws.challenge.exception.TransferSameAccountException;
import com.dws.challenge.service.AccountsService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	Logger log = LoggerFactory.getLogger(AccountsController.class);

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);
		
		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	// This method is defined for transferring the amount between accountIds

	@PostMapping(path = "/transferAmount", consumes = MediaType.APPLICATION_JSON_VALUE)
	public synchronized ResponseEntity<Object> amountTransfer(@RequestBody @Valid AmountTransferPojo amountTransfer) {
		try {

			this.accountsService.amountTransfer(amountTransfer.getAccountFrom(), amountTransfer.getAccountTo(),
					amountTransfer.getTransferAmount());

		} catch (AmountTransferPojoException | TransferSameAccountException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (AccountNotFoundException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
		} catch (NotSufficientBalanceException ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
		return new ResponseEntity<>("Transfer Completed", HttpStatus.ACCEPTED);
	}

}

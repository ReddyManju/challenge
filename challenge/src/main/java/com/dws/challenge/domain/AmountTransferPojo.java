package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// This class defines  properties  for Transferring amounts 

@Data
public class AmountTransferPojo {

	@NotNull
	private String accountFrom;

	@NotNull
	private String accountTo;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal transferAmount;

	@JsonCreator
	public AmountTransferPojo(@JsonProperty("accountFrom") String accountFrom,
			@JsonProperty("accountTo") String accountTo, @JsonProperty("transferAmount") BigDecimal transferAmount) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
		this.transferAmount = transferAmount;
	}

	public String getAccountFrom() {
		return accountFrom;
	}

	public void setAccountFrom(String accountFrom) {
		this.accountFrom = accountFrom;
	}

	public String getAccountTo() {
		return accountTo;
	}

	public void setAccountTo(String accountTo) {
		this.accountTo = accountTo;
	}

	public BigDecimal getTransferAmount() {
		return transferAmount;
	}

	public void setTransferAmount(BigDecimal transferAmount) {
		this.transferAmount = transferAmount;
	}
}
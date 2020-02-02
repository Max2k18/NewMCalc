package com.maxsavteam.newmcalc2.core;

import java.math.BigDecimal;

public class CalculationError {

	private String message = "";

	private String status = "";

	public CalculationError setStatus(String status){
		this.status = status;
		return this;
	}

	public String getStatus(){
		return status;
	}

	public String getMessage() {
		return message;
	}

	public CalculationError setMessage(String message) {
		this.message = message;
		return this;
	}

	public BigDecimal getPossibleResult() {
		return possibleResult;
	}

	private BigDecimal possibleResult;

	public CalculationError setPossibleResult(BigDecimal result){
		possibleResult = result;
		return this;
	}

	private String shortError = "";

	public String getShortError() {
		return shortError;
	}

	public CalculationError setShortError(String shortError) {
		this.shortError = shortError;
		return this;
	}

	private String errorMessage = "";

	public String getErrorMessage() {
		return errorMessage;
	}

	public CalculationError setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}
}

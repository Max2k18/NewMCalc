package com.maxsavteam.newmcalc.core;

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

	private String error = "";

	public String getError() {
		return error;
	}

	public CalculationError setError(String error) {
		this.error = error;
		return this;
	}
}

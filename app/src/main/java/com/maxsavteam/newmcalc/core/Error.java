package com.maxsavteam.newmcalc.core;

import java.math.BigDecimal;

public class Error {

	private String message = "";

	private String status = "";

	Error setStatus(String status){
		this.status = status;
		return this;
	}

	public String getStatus(){
		return status;
	}

	public String getMessage() {
		return message;
	}

	Error setMessage(String message) {
		this.message = message;
		return this;
	}

	BigDecimal getPossibleResult() {
		return possibleResult;
	}

	private BigDecimal possibleResult;
	Error setPossibleResult(BigDecimal result){
		possibleResult = result;
		return this;
	}

	private String shortError = "";

	public String getShortError() {
		return shortError;
	}

	Error setShortError(String shortError) {
		this.shortError = shortError;
		return this;
	}

	private String error = "";

	String getError() {
		return error;
	}

	Error setError(String error) {
		this.error = error;
		return this;
	}
}

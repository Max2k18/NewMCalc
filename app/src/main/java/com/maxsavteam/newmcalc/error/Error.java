package com.maxsavteam.newmcalc.error;

import java.math.BigDecimal;

public class Error {

	private String message = "";

	private String status = "";

	public Error setStatus(String status){
		this.status = status;
		return this;
	}

	public String getStatus(){
		return status;
	}

	public String getMessage() {
		return message;
	}

	public Error setMessage(String message) {
		this.message = message;
		return this;
	}

	public BigDecimal getPossibleResult() {
		return possibleResult;
	}

	private BigDecimal possibleResult;
	public Error setPossibleResult(BigDecimal result){
		possibleResult = result;
		return this;
	}

	private String shortError = "";

	public String getShortError() {
		return shortError;
	}

	public Error setShortError(String shortError) {
		this.shortError = shortError;
		return this;
	}

	private String error = "";

	public String getError() {
		return error;
	}

	public Error setError(String error) {
		this.error = error;
		return this;
	}
}

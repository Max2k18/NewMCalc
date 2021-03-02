package com.maxsavteam.newmcalc2.core;

public class CalculationError {

	private String message = "";
	private String status = "";
	private String shortError = "";

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

	public String getShortError() {
		return shortError;
	}

	public CalculationError setShortError(String shortError) {
		this.shortError = shortError;
		return this;
	}
}

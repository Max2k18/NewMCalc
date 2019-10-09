package com.maxsavteam.newmcalc.error;

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

	private String short_error = "";

	public String getShort_error() {
		return short_error;
	}

	public Error setShort_error(String short_error) {
		this.short_error = short_error;
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

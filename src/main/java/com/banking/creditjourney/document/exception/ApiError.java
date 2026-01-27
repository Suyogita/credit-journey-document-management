package com.banking.creditjourney.document.exception;

public class ApiError {
	private String message;
	private int status;

	
	public ApiError() {
		super();
	}


	public ApiError(String message, int status) {
		super();
		this.message = message;
		this.status = status;
	}
	
	

}

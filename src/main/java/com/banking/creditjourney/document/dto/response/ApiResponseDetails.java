package com.banking.creditjourney.document.dto.response;

public class ApiResponseDetails<T> {
	private boolean success;
	private T data;
	private String message;

	public ApiResponseDetails() {
		super();
	}

	public ApiResponseDetails(boolean success, T data, String message) {
		super();
		this.success = success;
		this.data = data;
		this.message = message;
	}

	public static <T> ApiResponseDetails<T> success(String message) {
		return new ApiResponseDetails<>(true, null, message);
	}

	public static <T> ApiResponseDetails<T> success(T data, String message) {
		return new ApiResponseDetails<>(true, data, message);
	}

	public static <T> ApiResponseDetails<T> failure(String message) {
		return new ApiResponseDetails<>(false, null, message);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

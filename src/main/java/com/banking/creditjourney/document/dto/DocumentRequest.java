package com.banking.creditjourney.document.dto;

public class DocumentRequest {
	private String userId;
	private String fileName;

	public DocumentRequest() {
		super();
	}

	public DocumentRequest(String userId, String fileName) {
		super();
		this.userId = userId;
		this.fileName = fileName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}

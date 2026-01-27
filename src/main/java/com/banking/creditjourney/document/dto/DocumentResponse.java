package com.banking.creditjourney.document.dto;

public class DocumentResponse {
	private Long documentId;
	private String message;
	private String fileName;

	public DocumentResponse(Long documentId, String message, String fileName) {
		super();
		this.documentId = documentId;
		this.message = message;
		this.fileName = fileName;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}

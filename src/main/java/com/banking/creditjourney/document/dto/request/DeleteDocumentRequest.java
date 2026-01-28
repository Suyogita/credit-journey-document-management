package com.banking.creditjourney.document.dto.request;

import java.util.List;

import com.banking.creditjourney.document.dto.response.DeleteType;

public class DeleteDocumentRequest {
	private List<Long> documentIds;
	private DeleteType deleteType; // 1.SOFT or 2.HARD
	private String reason;

	public DeleteDocumentRequest() {
		super();
	}

	public DeleteDocumentRequest(List<Long> documentIds, DeleteType deleteType, String reason) {
		super();
		this.documentIds = documentIds;
		this.deleteType = deleteType;
		this.reason = reason;
	}

	public List<Long> getDocumentIds() {
		return documentIds;
	}

	public void setDocumentIds(List<Long> documentIds) {
		this.documentIds = documentIds;
	}

	public DeleteType getDeleteType() {
		return deleteType;
	}

	public void setDeleteType(DeleteType deleteType) {
		this.deleteType = deleteType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	
	

}

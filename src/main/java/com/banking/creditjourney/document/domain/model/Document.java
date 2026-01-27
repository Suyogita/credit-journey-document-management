package com.banking.creditjourney.document.domain.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Document", description = "Represents a document uploaded by user in Document Management System")
public class Document {
	@Schema(description = "Unique identifier of the document")
	private Long documentId;

	@NotBlank(message = "User ID must not be blank")
	private String userId;

	@NotBlank(message = "File name must not be blank")
	private String fileName;

	@NotBlank(message = "File type must not be blank")
	@Schema(description = "MIME type of the document")
	private String fileType;

	@NotNull(message = "File size must be provided")
	@Schema(description = "Size of the document in bytes")
	private Long fileSize;

	@NotBlank(message = "Checksum must not be blank")
	@Schema(description = "SHA-256 checksum for duplicate detection")
	private String checksum;

	@NotBlank(message = "Storage path must not be blank")
	@Schema(description = "Path where the file is stored")
	private String storagePath;

	@Schema(description = "Indicates whether the document is soft deleted")
	private boolean fileDeleted = false;

	@Schema(description = "Timestamp when the document is created")
	private LocalDateTime createdAt;

	@Schema(description = "Timestamp when the document last updated")
	private LocalDateTime updatedAt;

	public Document() {
		super();
	}

	public Document(Long documentId, @NotBlank(message = "User ID must not be blank") String userId,
			@NotBlank(message = "File name must not be blank") String fileName,
			@NotBlank(message = "File type must not be blank") String fileType,
			@NotNull(message = "File size must be provided") Long fileSize,
			@NotBlank(message = "Checksum must not be blank") String checksum,
			@NotBlank(message = "Storage path must not be blank") String storagePath, boolean fileDeleted,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.documentId = documentId;
		this.userId = userId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.checksum = checksum;
		this.storagePath = storagePath;
		this.fileDeleted = fileDeleted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public boolean isFileDeleted() {
		return fileDeleted;
	}

	public void setFileDeleted(boolean fileDeleted) {
		this.fileDeleted = fileDeleted;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

}

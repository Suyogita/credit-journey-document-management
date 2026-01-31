package com.banking.creditjourney.document.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentListResponse {
	private Long documentId;
	private String fileName;
	private String fileType;
	private Long fileSize;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

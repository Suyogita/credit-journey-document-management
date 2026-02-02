package com.banking.creditjourney.document.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentListResponse {
	private Long documentId;
	private String fileName;
	private String fileType;
	private Long fileSize;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

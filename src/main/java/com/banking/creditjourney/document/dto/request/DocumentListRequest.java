package com.banking.creditjourney.document.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request parameters for listing documents with pagination, sorting and filtering")
public class DocumentListRequest {

	@Schema(description = "Page number (0-based)", example = "0")
	@Min(value = 0, message = "page must be >=0")
	private int page = 0;

	@Schema(description = "Page size", example = "10")
	@Min(value = 1, message = "page must be >=1")
	private int size = 10;

	@Schema(description = "Sort field", example = "created_at")
	private String sortBy = "created_at";

	@Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
	private String sortDir = "DESC";

	@Schema(description = "Filter documents created from this date (ISO format: yyyy-MM-dd)")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fromDate;

	@Schema(description = "Filter documents created till this date (ISO format: yyyy-MM-dd)")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate toDate;

	@Schema(description = "Minimum file size in bytes", example = "for 10KB put 10240 Bytes")
	private Long minSize;

	@Schema(description = "Maximum file size in bytes", example = "for 5 MB put 5,242,880 Bytes")
	private Long maxSize;
}

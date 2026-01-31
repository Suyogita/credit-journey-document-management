package com.banking.creditjourney.document.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentPagedResponse<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	
}

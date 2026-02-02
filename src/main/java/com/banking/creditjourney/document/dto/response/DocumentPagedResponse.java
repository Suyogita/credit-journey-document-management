package com.banking.creditjourney.document.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPagedResponse<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	
}

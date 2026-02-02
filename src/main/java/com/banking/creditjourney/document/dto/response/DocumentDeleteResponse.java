package com.banking.creditjourney.document.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDeleteResponse {
	private List<Long> documentIds;
	private DeleteType deleteType;
	private int deletedCount;
	private String deletedBy;
	private LocalDateTime deletedAt;
	

}

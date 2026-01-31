package com.banking.creditjourney.document.dto.request;

import java.util.List;

import com.banking.creditjourney.document.dto.response.DeleteType;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDocumentRequest {
	@NotEmpty(message = "documentIds must not be empty")
	private List<Long> documentIds;

	private DeleteType deleteType; // 1.SOFT or 2.HARD
	private String reason;

}

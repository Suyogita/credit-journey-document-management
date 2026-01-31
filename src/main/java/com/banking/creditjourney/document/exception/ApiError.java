package com.banking.creditjourney.document.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
	private String code;
	private String message;
	private LocalDateTime timestamp;

}

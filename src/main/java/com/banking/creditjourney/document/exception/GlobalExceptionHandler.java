
package com.banking.creditjourney.document.exception;

import java.time.LocalDateTime;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiError> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
		String message = "Request payload is missing or invalid";
		log.error("Business exception", ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		String message = "Request payload is missing or invalid";
		log.error("Business exception", ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		return buildError(HttpStatus.BAD_REQUEST, "Invalid request parameters");
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
		log.error("Business exception", ex);
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
		log.error("Business exception", ex);
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<ApiError> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
		String message = "Invalid documentId. Please enter valid documentId for download";
		log.error("Business exception", ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
	}

	private ResponseEntity<ApiError> buildError(HttpStatus status, String msg) {
		return ResponseEntity.status(status)
				.body(ApiError.builder().code(status.name()).message(msg).timestamp(LocalDateTime.now()).build());
	}
}

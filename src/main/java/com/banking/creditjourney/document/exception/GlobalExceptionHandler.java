
package com.banking.creditjourney.document.exception;

import java.time.LocalDateTime;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	public static final String DOCUMENT_NOT_FOUND = "Document not found";
	public static final String BUSINESS_EX = "Business exception";

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiError> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
		String message = "Request payload is missing or invalid";
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		String message = "Request payload is missing or invalid";
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<ApiError> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {

		log.error(DOCUMENT_NOT_FOUND, ex);
		return buildError(HttpStatus.NOT_FOUND, DOCUMENT_NOT_FOUND);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(DocumentNotFoundException.class)
	public ResponseEntity<ApiError> handleDocumentNotFoundException(DocumentNotFoundException ex) {
		log.error(DOCUMENT_NOT_FOUND, ex);
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
		log.error(BUSINESS_EX, ex);
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		return buildError(HttpStatus.BAD_REQUEST, "Invalid request parameters");
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

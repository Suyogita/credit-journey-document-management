package com.banking.creditjourney.document.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.DocumentRequest;
import com.banking.creditjourney.document.dto.DocumentResponse;
import com.banking.creditjourney.document.service.DocumentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("api/documentmgmt")
@Tag(name = "Credit Journey-Document Management")
public class DocumentController {
	private final DocumentServiceImpl documentService;

	public DocumentController(DocumentServiceImpl documentService) {
		super();
		this.documentService = documentService;
	}

	@Operation(summary = "Upload the document in Local File System", description = "Upload one or multiple PDF files with metadata")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error"),
			@ApiResponse(responseCode = "409", description = "Duplicate file"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@PostMapping(value = "/documentUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DocumentResponse>> documentUpload(
			@Parameter(description = "PDF files to upload", required = true, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) @RequestPart("files") List<MultipartFile> files,
			@Parameter(description = "Document metadata", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart("request") DocumentRequest request) {
		return ResponseEntity.ok(documentService.uploadFiles(files, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> documentDelete(@PathVariable Long id) {
		return ResponseEntity.ok("Delete API Skeleton");
	}

	@GetMapping
	public ResponseEntity<String> getDocumentList() {
		return ResponseEntity.ok("List API Skeleton");
	}

}

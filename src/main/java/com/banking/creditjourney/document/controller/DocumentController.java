package com.banking.creditjourney.document.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.ApiResponseDetails;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.dto.response.DocumentResponse;
import com.banking.creditjourney.document.security.UserContext;
import com.banking.creditjourney.document.service.DocumentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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

	/*
	 * REST endpoint to upload single or multiple PDF document(s) at Local File
	 * System and save file metadata into DB.
	 *
	 */
	@Operation(summary = "Upload single or multiple PDF document(s) at Local File System and save file metadata into DB", description = "Upload one or multiple PDF files with metadata")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error"),
			@ApiResponse(responseCode = "409", description = "Duplicate file"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@PostMapping(value = "/documentUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DocumentResponse>> documentUploads(
			@Parameter(description = "PDF files to upload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("files") List<MultipartFile> files,
			@Parameter(description = "Document metadata", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart(value = "request", required = false) String requestJson)
			throws IOException {

		String user = UserContext.getUserId();
		log.info("Document upload API starts:  /documentUpload | userId={} | filecount={}", user, files.size());

		ObjectMapper mapper = new ObjectMapper();
		CreateDocumentRequest request = null;
		if (requestJson != null && !requestJson.isBlank()) {
			request = mapper.readValue(requestJson, CreateDocumentRequest.class);
		}

		List<DocumentResponse> resp = documentService.uploadFiles(files, request, user);
		log.info("Upload API completed | userId={}", user);
		return ResponseEntity.ok(resp);
	}

	/*
	 * REST endpoint to delete single or multiple PDF document(s) with audit trail
	 * Functionality includes Soft/Hard delete.
	 */
	@Operation(summary = "Delete PDF document(s)", description = "Document Soft/Hard delete with audit trail")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "PDF document(s) deleted successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@DeleteMapping(value = "/documentsDelete")
	public ResponseEntity<ApiResponseDetails<Void>> documentDelete(@RequestBody DeleteDocumentRequest request) {
		String user = UserContext.getUserId();
		log.info("Document delete API starts:  /documentsDelete | request{} | userId{}", request, user);
		documentService.documentDeletes(request, user);
		ApiResponseDetails<Void> response = new ApiResponseDetails<>();
		response.setSuccess(true);
		response.setMessage("Document(s) deleted successfully");
		response.setData(null);
		return ResponseEntity.ok(response);

	}

	/*
	 * REST endpoint for document listing Functionality includes
	 * pagination,sorting,filtering,user isolation
	 */
	@Operation(summary = "List PDF documents", description = "Includes Pagination,Sorting and Filtering")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = ""),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@GetMapping(value = "/documentsListing")
	public ResponseEntity<DocumentPagedResponse<DocumentListResponse>> listDocuments(
			@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "10") @Min(1) int size,
			@RequestParam(defaultValue = "created_at") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(required = false) Long minSize, @RequestParam(required = false) Long maxSize) {
		log.info("Document listing API starts:  /documentsListing ");
		String user = UserContext.getUserId();
		log.info("Listing documents for user{}", user);

		return ResponseEntity.ok(
				documentService.listDocuments(user, page, size, sortBy, sortDir, fromDate, toDate, minSize, maxSize));
	}

	/*
	 * REST endpoint for document retrieval(streaming) depending on documentid with
	 * validation(ownership)
	 *
	 */
	@Operation(summary = "Get PDF document by documentid", description = "Retrieve single document metadata for logged-in user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Document fetched successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@GetMapping(value = "/documentsDownload/{documentId}")
	public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {

		String user = UserContext.getUserId();
		log.info("Get Document API /documents/{documentId} starts for userId{}", user);

		return documentService.downloadDocument(documentId, user);
	}
}

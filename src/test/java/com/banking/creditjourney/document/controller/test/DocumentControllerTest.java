package com.banking.creditjourney.document.controller.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.banking.creditjourney.document.controller.v1.DocumentController;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.DeleteType;
import com.banking.creditjourney.document.dto.response.DocumentDeleteResponse;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.dto.response.DocumentResponse;
import com.banking.creditjourney.document.exception.DocumentNotFoundException;
import com.banking.creditjourney.document.service.DocumentService;
import com.banking.creditjourney.document.service.DocumentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DocumentService documentServices;

	@MockBean
	private DocumentServiceImpl documentService;

	@Autowired
	private ObjectMapper objectMapper;

	private void mockAuthenticatedUser(String userId) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
		SecurityContextHolder.setContext(context);
	}

	private MockMultipartFile validFile() {
		return new MockMultipartFile("files", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes());
	}

	private MockMultipartFile requestPart() throws Exception {
		CreateDocumentRequest request = new CreateDocumentRequest("test.pdf");
		return new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsBytes(request));
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void upload_success() throws Exception {
		mockAuthenticatedUser("user1");

		Mockito.when(documentService.uploadFiles(anyList(), any(), eq("user1")))
				.thenReturn(List.of(new DocumentResponse(1L, "SUCCESS", "test.pdf")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isOk()).andExpect(jsonPath("$[0].documentId").value(1L))
				.andExpect(jsonPath("$[0].fileName").value("test.pdf"));
	}

	@Test
	void upload_missingFiles() throws Exception {
		mockAuthenticatedUser("user1");

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documentmgmt/documentUpload").file(requestPart())
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request payload is missing or invalid"));
	}

	@Test
	void upload_missingRequest() throws Exception {
		mockAuthenticatedUser("user1");

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documentmgmt/documentUpload").file(validFile())
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request payload is missing or invalid"));

	}

	@Test
	void upload_missingUserContext() throws Exception {

		SecurityContextHolder.clearContext();

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("User not authenticated"));
	}

	@Test
	void upload_serviceException() throws Exception {
		mockAuthenticatedUser("user1");

		Mockito.when(documentService.uploadFiles(anyList(), any(), anyString()))
				.thenThrow(new RuntimeException("DB error"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
	}

	// Delete api

	@Test
	void deleteDocument_softDelete_success() throws Exception {
		mockAuthenticatedUser("user1");
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L, 2L), DeleteType.SOFT, "cleanup");

		DocumentDeleteResponse serviceResponse = new DocumentDeleteResponse(List.of(1L, 2L), DeleteType.SOFT, 2,
				"user1", LocalDateTime.now());

		when(documentService.documentDeletes(any(), eq("user1"))).thenReturn(serviceResponse);

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.deletedCount").value(2))
				.andExpect(jsonPath("$.message").value("Document(s) deleted successfully"));
	}

	@Test
	void deleteDocument_hardDelete_success() throws Exception {
		mockAuthenticatedUser("user1");
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(5L), DeleteType.HARD, "purge");

		when(documentService.documentDeletes(any(), eq("user1")))
				.thenReturn(new DocumentDeleteResponse(List.of(5L), DeleteType.HARD, 1, "user1", LocalDateTime.now()));

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.deletedCount").value(1));
	}

	@Test
	void deleteDocument_emptyDocumentIds_shouldReturn400() throws Exception {
		mockAuthenticatedUser("user1");

		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(), DeleteType.SOFT, "cleanup");

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void deleteDocument_invalidDeleteType_shouldReturn400() throws Exception {
		String invalidJson = """
				{
				  "documentIds":[1],
				  "deleteType":"INVALID",
				  "reason":"test"
				}
				""";

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson)).andExpect(status().isBadRequest());
	}

	@Test
	void deleteDocument_serviceThrowsIllegalArgument_shouldReturn500() throws Exception {
		mockAuthenticatedUser("user1");

		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), DeleteType.SOFT, "cleanup");

		when(documentService.documentDeletes(any(), eq("user1")))
				.thenThrow(new IllegalArgumentException("Already deleted"));

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void deleteDocument_serviceThrowsRuntimeException_shouldReturn500() throws Exception {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), DeleteType.SOFT, "cleanup");

		when(documentService.documentDeletes(any(), any())).thenThrow(new RuntimeException("DB down"));

		mockMvc.perform(delete("/api/v1/documentmgmt/documentsDelete").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isInternalServerError());
	}

	// list document

	@Test
	void listDocuments_defaultRequest_shouldReturn200() throws Exception {
		mockAuthenticatedUser("user1");
		when(documentService.listDocuments(anyString(), any())).thenReturn(emptyPage());

		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing")).andExpect(status().isOk());
	}

	@Test
	void listDocuments_withAllParams_shouldReturn200() throws Exception {
		mockAuthenticatedUser("user1");
		when(documentService.listDocuments(anyString(), any())).thenReturn(samplePage());

		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing").param("page", "1").param("size", "5")
				.param("sortBy", "created_at").param("sortDir", "DESC").param("fromDate", "2024-01-01")
				.param("toDate", "2024-12-31").param("minSize", "100").param("maxSize", "1000"))
				.andExpect(status().isOk());
	}

	@Test
	void listDocuments_invalidPage_shouldReturn400() throws Exception {
		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing").param("page", "-1"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listDocuments_invalidSize_shouldReturn400() throws Exception {
		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing").param("size", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listDocuments_invalidSortBy_shouldReturn400() throws Exception {
		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing").param("sortBy", "hack_column"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listDocuments_serviceThrowsException_shouldReturn500() throws Exception {
		mockAuthenticatedUser("user1");
		when(documentService.listDocuments(anyString(), any())).thenThrow(new RuntimeException("DB error"));

		mockMvc.perform(get("/api/v1/documentmgmt/documentsListing").param("page", "0").param("size", "10"))
				.andExpect(status().isInternalServerError());
	}

	private DocumentPagedResponse<DocumentListResponse> emptyPage() {
		return DocumentPagedResponse.<DocumentListResponse>builder().content(List.of()).page(0).size(10)
				.totalElements(0).build();
	}

	private DocumentPagedResponse<DocumentListResponse> samplePage() {
		return DocumentPagedResponse.<DocumentListResponse>builder().content(List.of(new DocumentListResponse()))
				.page(0).size(10).totalElements(1).build();
	}

	// retrieve document

	@Test
	void shouldDownloadDocumentSuccessfully() throws Exception {

		Long documentId = 1L;

		mockAuthenticatedUser("user1");
		byte[] pdfBytes = "pdf-content".getBytes();

		Resource resource = new ByteArrayResource(pdfBytes);

		ResponseEntity<Resource> response = ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"doc.pdf\"").body(resource);

		Mockito.when(documentService.downloadDocument(documentId, "user1")).thenReturn(response);

		mockMvc.perform(get("/api/v1/documentmgmt/documentsDownload/{documentId}", documentId)).andExpect(status().isOk())
				.andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_PDF))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"doc.pdf\""));
	}

	@Test
	void shouldReturn404WhenDocumentNotFound() throws Exception {

		mockAuthenticatedUser("user1");

		Mockito.when(documentService.downloadDocument(99L, "user1"))
				.thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

		mockMvc.perform(get("/api/v1/documentmgmt/documentsDownload/{documentId}", 99L)).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn404WhenFileMissingOnDisk() throws Exception {
		mockAuthenticatedUser("user1");

		Mockito.when(documentService.downloadDocument(1L, "user1"))
				.thenThrow(new DocumentNotFoundException("FILE_NOT_FOUND_ON_DISK"));

		mockMvc.perform(get("/api/v1/documentmgmt/documentsDownload/{documentId}", 1L)).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn400ForInvalidDocumentId() throws Exception {
		mockAuthenticatedUser("user1");

		mockMvc.perform(get("/api/v1/documentmgmt/documentsDownload/{documentId}", "abc"))
				.andExpect(status().isBadRequest());
	}

}

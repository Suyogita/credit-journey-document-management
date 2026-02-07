package com.banking.creditjourney.document.controller.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.banking.creditjourney.document.controller.DocumentController;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.response.DocumentResponse;
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

	// -------------------- helpers --------------------

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

	// -------------------- TESTS --------------------

	@Test
	void upload_success() throws Exception {
		mockAuthenticatedUser("user1");

		Mockito.when(documentService.uploadFiles(anyList(), any(), eq("user1")))
				.thenReturn(List.of(new DocumentResponse(1L, "SUCCESS", "test.pdf")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isOk()).andExpect(jsonPath("$[0].documentId").value(1L))
				.andExpect(jsonPath("$[0].fileName").value("test.pdf"));
	}

	@Test
	void upload_missingFiles() throws Exception {
		mockAuthenticatedUser("user1");

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documentmgmt/documentUpload").file(requestPart())
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request payload is missing or invalid"));
	}

	@Test
	void upload_missingRequest() throws Exception {
		mockAuthenticatedUser("user1");

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documentmgmt/documentUpload").file(validFile())
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request payload is missing or invalid"));

	}

	@Test
	void upload_missingUserContext() throws Exception {

		SecurityContextHolder.clearContext();

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("User not authenticated"));
	}

	@Test
	void upload_serviceException() throws Exception {
		mockAuthenticatedUser("user1");

		Mockito.when(documentService.uploadFiles(anyList(), any(), anyString()))
				.thenThrow(new RuntimeException("DB error"));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documentmgmt/documentUpload").file(validFile())
				.file(requestPart())).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
	}
}

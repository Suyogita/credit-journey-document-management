//package com.banking.creditjourney.document.controller.test;
//
////import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.banking.creditjourney.document.controller.DocumentController;
//import com.banking.creditjourney.document.dto.request.DocumentListRequest;
//import com.banking.creditjourney.document.dto.response.DocumentListResponse;
//import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
//import com.banking.creditjourney.document.service.DocumentService;
//import com.banking.creditjourney.document.service.DocumentServiceImpl;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@WebMvcTest(DocumentController.class)
//@AutoConfigureMockMvc(addFilters = false)
//public class DocumentAPIContractIT {
//	@Autowired
//	private MockMvc mockMvc;
//
//	@MockBean
//	private DocumentService documentServices;
//
//	@MockBean
//	private DocumentServiceImpl documentService;
//
//	@Autowired
//	private ObjectMapper objectMapper;
//
//	private void mockAuthenticatedUser(String userId) {
//		SecurityContext context = SecurityContextHolder.createEmptyContext();
//		context.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
//		SecurityContextHolder.setContext(context);
//	}
//
//	@AfterEach
//	void clearContext() {
//		SecurityContextHolder.clearContext();
//	}
//
//	// ---------------------------------------------------
//	// CONTRACT TEST 1: Happy path
//	// ---------------------------------------------------
//	@Test
//	void listDocuments_contract_shouldReturnPagedResponse() throws Exception {
//
//		// Arrange
//		mockAuthenticatedUser("user1");
//
//		DocumentListResponse doc = DocumentListResponse.builder().documentId(1L).fileName("test.pdf").fileType("PDF")
//				.fileSize(1024L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
//
//		DocumentPagedResponse<DocumentListResponse> response = DocumentPagedResponse.<DocumentListResponse>builder()
//				.content(List.of(doc)).page(0).size(10).totalElements(1L).build();
//
//		Mockito.when(documentService.listDocuments(Mockito.eq("user1"), Mockito.any(DocumentListRequest.class)))
//				.thenReturn(response);
//
//		// Act + Assert (CONTRACT)
//		mockMvc.perform(get("/api/documentmgmt/documentsListing").param("page", "0").param("size", "10")
//				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
//
//				// ---- CONTRACT ASSERTIONS ----
//				.andExpect(jsonPath("$.content").isArray()).andExpect(jsonPath("$.content[0].documentId").value(1))
//				.andExpect(jsonPath("$.content[0].fileName").value("test.pdf"))
//				.andExpect(jsonPath("$.content[0].fileType").value("PDF")).andExpect(jsonPath("$.page").value(0))
//				.andExpect(jsonPath("$.size").value(10)).andExpect(jsonPath("$.totalElements").value(1));
//	}
//
//	// ---------------------------------------------------
//	// CONTRACT TEST 2: User missing → error contract
//	// ---------------------------------------------------
//	@Test
//	void listDocuments_contract_userMissing_shouldReturn400() throws Exception {
//
//		// UserContext NOT set
//
//		mockMvc.perform(get("/api/documentmgmt/documentsListing").param("page", "0").param("size", "10")
//				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.message").exists());
//	}
//
//	// ---------------------------------------------------
//	// CONTRACT TEST 3: Invalid pagination parameters
//	// ---------------------------------------------------
//	@Test
//	void listDocuments_contract_invalidPagination_shouldReturn400() throws Exception {
//		mockAuthenticatedUser("user1");
//
//		mockMvc.perform(get("/api/documentmgmt/documentsListing").param("page", "-1").param("size", "0")
//				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//	}
//
//}

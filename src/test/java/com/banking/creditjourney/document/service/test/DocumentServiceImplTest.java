package com.banking.creditjourney.document.service.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.request.DocumentListRequest;
import com.banking.creditjourney.document.dto.response.DeleteType;
import com.banking.creditjourney.document.dto.response.DocumentDeleteResponse;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;
import com.banking.creditjourney.document.repository.AuditRepository;
import com.banking.creditjourney.document.repository.DocumentRepository;
import com.banking.creditjourney.document.service.DocumentServiceImpl;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private AuditRepository auditRepository;

	@Mock
	private DocumentHelper documentHelper;

	@InjectMocks
	private DocumentServiceImpl documentService;

	private MultipartFile file;
	private CreateDocumentRequest request;
	private final String USER = "user1";

	@BeforeEach
	void setup() {
		file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());

		request = new CreateDocumentRequest();
	}

	@Test
	void uploadFiles_success() {
		when(documentRepository.findByCheckSum(any())).thenReturn(Optional.empty());

		when(documentHelper.generateChecksumForFile(file)).thenReturn("checksum");

		when(documentHelper.storeFile(any(), any())).thenReturn("/path/test.pdf");

		when(documentHelper.prepareDocumentObject(any(), any(), any(), any(), any())).thenReturn(new Document());

		when(documentRepository.saveDocumentIntoDB(any())).thenReturn(1L);

		assertDoesNotThrow(() -> documentService.uploadFiles(List.of(file), request, "user1"));
	}

	@Test
	void uploadFiles_emptyFiles_shouldNotFail() {

		assertDoesNotThrow(() -> documentService.uploadFiles(List.of(), request, "user1"));
		verify(documentRepository, never()).saveDocumentIntoDB(any());

	}

//	@Test
//	void uploadFiles_duplicateFile_shouldFail() {
//		when(documentHelper.generateChecksumForFile(file)).thenReturn("checksum");
//
//		when(documentRepository.findByCheckSum("checksum")).thenReturn(Optional.of(new Document()));
//
//		RuntimeException ex = assertThrows(RuntimeException.class,
//				() -> documentService.uploadFiles(List.of(file), request, "user1"));
//
//		assertEquals(DocumentGlobalConstants.DUPLICATE_FILE, ex.getMessage());
//	}

	@Test
	void uploadFiles_duplicateFile_shouldSkip() {
		when(documentHelper.generateChecksumForFile(file)).thenReturn("checksum");

		when(documentRepository.findByCheckSum("checksum")).thenReturn(Optional.of(new Document()));

		assertDoesNotThrow(() -> documentService.uploadFiles(List.of(file), request, "user1"));

		verify(documentRepository, never()).saveDocumentIntoDB(any());

	}

	@Test
	void uploadFiles_storageFailure_shouldFail() {
		when(documentHelper.generateChecksumForFile(file)).thenReturn("checksum");

		when(documentRepository.findByCheckSum(any())).thenReturn(Optional.empty());

		when(documentHelper.storeFile(any(), any()))
				.thenThrow(new RuntimeException(DocumentGlobalConstants.FILE_STORAGE_FAILED));

		assertThrows(RuntimeException.class, () -> documentService.uploadFiles(List.of(file), request, "user1"));
	}

	@Test
	void uploadFiles_auditFailure_shouldFail() {
		when(documentRepository.findByCheckSum(any())).thenReturn(Optional.empty());

		when(documentHelper.generateChecksumForFile(file)).thenReturn("checksum");

		when(documentHelper.storeFile(any(), any())).thenReturn("/path");

		when(documentHelper.prepareDocumentObject(any(), any(), any(), any(), any())).thenReturn(new Document());

		when(documentRepository.saveDocumentIntoDB(any())).thenReturn(1L);

		doThrow(new RuntimeException("audit error")).when(auditRepository).saveAudit(anyLong(), any(), any(), any());

		assertThrows(RuntimeException.class, () -> documentService.uploadFiles(List.of(file), request, "user1"));
	}

	// delete

	@Test
	void delete_documentNotFound_shouldFail() {
		when(documentRepository.findByIds(List.of(99L), "user1")).thenReturn(List.of());

		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(99L), DeleteType.SOFT, "cleanup");

		assertThrows(IllegalArgumentException.class, () -> documentService.documentDeletes(request, "user1"));
	}

	@Test
	void delete_documentBelongsToAnotherUser_shouldFail() {
		when(documentRepository.findByIds(List.of(1L), "user1"))
				.thenThrow(new IllegalArgumentException("DOCUMENT_NOT_BELONGS_TO_USER"));

		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), DeleteType.SOFT, "cleanup");

		assertThrows(IllegalArgumentException.class, () -> documentService.documentDeletes(request, "user1"));
	}

	@Test
	void softDelete_success() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L, 2L), DeleteType.SOFT, "cleanup");

		Document d1 = new Document();
		d1.setDocumentId(1L);
		d1.setFileDeleted(false);

		Document d2 = new Document();
		d2.setDocumentId(2L);
		d2.setFileDeleted(false);

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(d1, d2));

		when(documentRepository.softDeleteByIds(List.of(1L, 2L), USER)).thenReturn(2);

		DocumentDeleteResponse response = documentService.documentDeletes(request, USER);

		assertEquals(2, response.getDeletedCount());

		verify(auditRepository, times(2)).saveAudit(anyLong(), eq("SOFT_DELETE"), eq(USER), eq("cleanup"));

		verify(documentRepository).softDeleteByIds(List.of(1L, 2L), USER);

		verify(documentRepository, never()).hardDeleteByIds(any());
	}

	@Test
	void hardDelete_success() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(5L), DeleteType.HARD, "purge");

		Document softDeletedDoc = new Document();
		softDeletedDoc.setDocumentId(5L);
		softDeletedDoc.setFileDeleted(true);
		softDeletedDoc.setStoragePath("/tmp/test.pdf");

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(softDeletedDoc));

		when(documentRepository.hardDeleteByIds(List.of(5L))).thenReturn(1);

		DocumentDeleteResponse response = documentService.documentDeletes(request, USER);

		assertEquals(1, response.getDeletedCount());

		verify(documentHelper).deleteFileFromLocal("/tmp/test.pdf", null);

		verify(auditRepository).saveAudit(5L, "HARD_DELETE", USER, "purge");

		verify(documentRepository).hardDeleteByIds(List.of(5L));
	}

	@Test
	void noDocumentsFound_shouldThrowException() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), DeleteType.SOFT, "cleanup");

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> documentService.documentDeletes(request, USER));

		assertEquals(DocumentGlobalConstants.NO_DOCUMENT_FOUND, ex.getMessage());
	}

	@Test
	void documentBelongsToAnotherUser_shouldThrowException() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L, 2L), DeleteType.SOFT, "cleanup");

		Document d1 = new Document();
		d1.setFileDeleted(false);

		// Only 1 document returned, but 2 requested
		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(d1));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> documentService.documentDeletes(request, USER));

		assertEquals(DocumentGlobalConstants.DOCUMENT_NOT_BELONGS_TO_USER, ex.getMessage());
	}

	@Test
	void softDelete_alreadyDeleted_shouldThrowException() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), DeleteType.SOFT, "cleanup");

		Document deletedDoc = new Document();
		deletedDoc.setFileDeleted(true);

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(deletedDoc));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> documentService.documentDeletes(request, USER));

		assertEquals(DocumentGlobalConstants.DOCUMENT_DELETED_ALREADY, ex.getMessage());
	}

	@Test
	void hardDelete_withoutSoftDelete_shouldThrowException() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(10L), DeleteType.HARD, "purge");

		Document activeDoc = new Document();
		activeDoc.setFileDeleted(false);

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(activeDoc));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> documentService.documentDeletes(request, USER));

		assertEquals(DocumentGlobalConstants.DOCUMENT_SOFT_FIRST_BEFORE_HARD, ex.getMessage());
	}

	@Test
	void invalidDeleteType_shouldThrowException() {
		DeleteDocumentRequest request = new DeleteDocumentRequest(List.of(1L), null, "cleanup");

		Document doc = new Document();
		doc.setFileDeleted(false);

		when(documentRepository.findByIds(request.getDocumentIds(), USER)).thenReturn(List.of(doc));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> documentService.documentDeletes(request, USER));

		assertEquals(DocumentGlobalConstants.INVALID_DELETETYPE, ex.getMessage());
	}

	// listing document
	@Test
	void listDocuments_successFlow_shouldReturnPagedResponse() {

		when(documentRepository.listDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong(), anyString(), anyString(), anyInt(), anyInt()))
				.thenReturn(List.of(new DocumentListResponse()));

		when(documentRepository.countDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong())).thenReturn(1L);

		DocumentListRequest request = DocumentListRequest.builder().page(0).size(10).build();

		DocumentPagedResponse<DocumentListResponse> response = documentService.listDocuments("user123", request);

		assertEquals(1, response.getTotalElements());
		assertEquals(1, response.getContent().size());
		verify(documentRepository).listDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong(), anyString(), anyString(), eq(10), eq(0));
	}

	@Test
	void listDocuments_emptyResult_shouldReturnEmptyPage() {

		when(documentRepository.listDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong(), anyString(), anyString(), anyInt(), anyInt())).thenReturn(List.of());

		when(documentRepository.countDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong())).thenReturn(0L);

		DocumentPagedResponse<DocumentListResponse> response = documentService.listDocuments("user123",
				DocumentListRequest.builder().build());

		assertTrue(response.getContent().isEmpty());
		assertEquals(0, response.getTotalElements());
	}

	@Test
	void listDocuments_repositoryThrowsException_shouldPropagate() {

		when(documentRepository.listDocuments(anyString(), any(LocalDate.class), any(LocalDate.class), anyLong(),
				anyLong(), anyString(), anyString(), anyInt(), anyInt())).thenThrow(new RuntimeException("DB failure"));

		assertThrows(RuntimeException.class,
				() -> documentService.listDocuments("user123", DocumentListRequest.builder().build()));
	}

}

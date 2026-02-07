package com.banking.creditjourney.document.service.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}

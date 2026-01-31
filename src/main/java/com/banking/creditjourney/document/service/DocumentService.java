package com.banking.creditjourney.document.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.dto.response.DocumentResponse;

public interface DocumentService {

	void documentDeletes(DeleteDocumentRequest request, String user);

	DocumentPagedResponse<DocumentListResponse> listDocuments(String user, int page, int size, String sortBy,
			String sortDir, LocalDate fromDate, LocalDate toDate, Long minSize, Long maxSize);

	ResponseEntity<Resource> downloadDocument(Long documentId, String user);

	List<DocumentResponse> uploadFiles(List<MultipartFile> files, CreateDocumentRequest request, String user);
}

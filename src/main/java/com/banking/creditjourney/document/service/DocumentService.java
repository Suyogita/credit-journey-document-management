package com.banking.creditjourney.document.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.request.DocumentListRequest;
import com.banking.creditjourney.document.dto.response.DocumentDeleteResponse;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.dto.response.DocumentResponse;

public interface DocumentService {

	DocumentDeleteResponse documentDeletes(DeleteDocumentRequest request, String user);

	DocumentPagedResponse<DocumentListResponse> listDocuments(String user, DocumentListRequest request);

	ResponseEntity<Resource> downloadDocument(Long documentId, String user);

	List<DocumentResponse> uploadFiles(List<MultipartFile> files, CreateDocumentRequest request, String user);
}

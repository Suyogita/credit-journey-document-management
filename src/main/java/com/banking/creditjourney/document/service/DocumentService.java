package com.banking.creditjourney.document.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.DocumentResponse;

public interface DocumentService {

	List<DocumentResponse> uploadFiles(List<MultipartFile> files, CreateDocumentRequest request);

	void documentDeletes(DeleteDocumentRequest request, String user);

}

package com.banking.creditjourney.document.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.dto.DocumentRequest;
import com.banking.creditjourney.document.dto.DocumentResponse;

public interface DocumentService {
	
	List<DocumentResponse> uploadFiles(List<MultipartFile> files,DocumentRequest request);

}

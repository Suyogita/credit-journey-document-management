package com.banking.creditjourney.document.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.DocumentRequest;
import com.banking.creditjourney.document.dto.DocumentResponse;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;
import com.banking.creditjourney.document.repository.DocumentRepository;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentHelper documentHelper;

	public DocumentServiceImpl(DocumentRepository documentRepository, DocumentHelper documentHelper) {
		super();
		this.documentRepository = documentRepository;
		this.documentHelper = documentHelper;
	}

	@Override
	public List<DocumentResponse> uploadFiles(List<MultipartFile> files, DocumentRequest request) {
		List<DocumentResponse> responses = new ArrayList<>();
		for (MultipartFile file : files) {
			// validate the file
			documentHelper.validateFileBeforeUpload(file);

			// generate checksum for file
			String checkSumString = documentHelper.generateChecksumForFile(file);

			// Duplicate file check
			Optional<Document> existingDoc = documentRepository.findByCheckSum(checkSumString);
			if (existingDoc.isPresent()) {
				responses.add(
						new DocumentResponse(null, file.getOriginalFilename(), DocumentGlobalConstants.DUPLICATE_FILE));
				continue;
			}

			// Store file on disk
			String fileStoragePath = documentHelper.storeFile(file, request.getUserId());

			// Save metadata to H2 DB
			Document prepareDocumentToSave = documentHelper.prepareDocumentObject(fileStoragePath, request, file,
					checkSumString);
			Long savedDocumentId = documentRepository.saveDocumentIntoDB(prepareDocumentToSave);

			responses.add(new DocumentResponse(savedDocumentId, DocumentGlobalConstants.FILE_UPLOAD_SUCCESS,
					fileStoragePath));
		}
		return responses;
	}

	public void delete(Long id) {
		// week 2
	}

}

package com.banking.creditjourney.document.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.DeleteType;
import com.banking.creditjourney.document.dto.response.DocumentResponse;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;
import com.banking.creditjourney.document.repository.AuditRepository;
import com.banking.creditjourney.document.repository.DocumentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DocumentServiceImpl implements DocumentService {

	private final DocumentRepository documentRepository;
	private final DocumentHelper documentHelper;
	private final AuditRepository auditRepository;

	public DocumentServiceImpl(DocumentRepository documentRepository, DocumentHelper documentHelper,
			AuditRepository auditRepository) {
		super();
		this.documentRepository = documentRepository;
		this.documentHelper = documentHelper;
		this.auditRepository = auditRepository;
	}

	@Override
	public List<DocumentResponse> uploadFiles(List<MultipartFile> files, CreateDocumentRequest request) {
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

	@Override
	@Transactional
	public void documentDeletes(DeleteDocumentRequest request, String user) {

		// Log.info("Delete document(s) start: deleteDocumentRequest={}", request);

		List<Long> documentIds = request.getDocumentIds();

		// Fetch documents
		List<Document> documents = documentRepository.findByIds(documentIds);

		if (documents.isEmpty()) {
			throw new IllegalArgumentException(DocumentGlobalConstants.NO_DOCUMENT_FOUND);
		}

		// Audit trail first
		for (Document document : documents) {
			auditRepository.saveAudit(document.getDocumentId(), request.getDeleteType().name() + "_DELETE", user,
					request.getReason());
		}
		// Perform soft delete
		if (request.getDeleteType() == DeleteType.SOFT) {
			documentRepository.softDeleteByIds(documentIds, user);
		} else {
			// HARD delete → file system first
			for (Document document : documents) {
				documentHelper.deleteFileFromLocal(document.getStoragePath());
			}
			// HARD delete → db
			documentRepository.hardDeleteByIds(documentIds);
		}
	}

}

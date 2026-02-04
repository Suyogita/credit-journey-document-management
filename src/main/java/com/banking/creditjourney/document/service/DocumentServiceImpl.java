package com.banking.creditjourney.document.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.request.DocumentListRequest;
import com.banking.creditjourney.document.dto.response.DeleteType;
import com.banking.creditjourney.document.dto.response.DocumentDeleteResponse;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
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
	@Value("${document.storage.base.path}")
	private String storageBasePath;

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
	public List<DocumentResponse> uploadFiles(List<MultipartFile> files, CreateDocumentRequest request, String user) {
		log.info("Inside uploadFiles() ");
		log.info("Starting document upload ");
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
			String fileStoragePath = documentHelper.storeFile(file, user);

			// Save metadata to H2 DB
			Document prepareDocumentToSave = documentHelper.prepareDocumentObject(fileStoragePath, request, file,
					checkSumString, user);
			Long savedDocumentId = documentRepository.saveDocumentIntoDB(prepareDocumentToSave);

			// Audit trail first, add entry of document in audit trail table

			auditRepository.saveAudit(savedDocumentId, "FILE UPLOAD", user, "New PDF file uploaded");

			responses.add(new DocumentResponse(savedDocumentId, DocumentGlobalConstants.FILE_UPLOAD_SUCCESS,
					fileStoragePath));
		}
		return responses;
	}

	@Override
	@Transactional
	public DocumentDeleteResponse documentDeletes(DeleteDocumentRequest request, String user) {
		log.info("Delete document(s) start: deleteDocumentRequest={} | user={} |deleteType={}", request, user,
				request.getDeleteType());

		List<Document> deletedDocs;
		int deletedCount = 0;

		List<Long> documentIds = request.getDocumentIds();

		// Fetch documents only for logged-in user
		List<Document> documents = documentRepository.findByIds(documentIds, user);
		log.info("Fetched documents from DB= | requested={} | found={} ", request.getDocumentIds().size(),
				documents.size());

		if (documents.isEmpty()) {
			throw new IllegalArgumentException(DocumentGlobalConstants.NO_DOCUMENT_FOUND);
		}

		if (documents.size() != documentIds.size()) {
			// Some Ids belong to other users or don't exist
			throw new IllegalArgumentException(DocumentGlobalConstants.DOCUMENT_NOT_BELONGS_TO_USER);
		}

		// split documents
		List<Document> activeOrNonDeletedDocs = documents.stream().filter(doc -> !doc.isFileDeleted()).toList();

		List<Document> alreadySoftDeletedDocs = documents.stream().filter(Document::isFileDeleted).toList();

		log.info("Document split| active(non-deleted)={} | softDeleted=+{} ", activeOrNonDeletedDocs.size(),
				alreadySoftDeletedDocs.size());

		if (request.getDeleteType() == DeleteType.SOFT) {

			log.info("Soft delete initiared..");
			if (activeOrNonDeletedDocs.isEmpty()) {
				throw new IllegalArgumentException(DocumentGlobalConstants.DOCUMENT_DELETED_ALREADY);
			}
			// Audit trail first, only active documents
			for (Document document : activeOrNonDeletedDocs) {
				auditRepository.saveAudit(document.getDocumentId(), request.getDeleteType().name() + "_DELETE", user,
						request.getReason());
			}

			List<Long> activeIds = activeOrNonDeletedDocs.stream().map(Document::getDocumentId).toList();
			log.info("Soft delete initiared for DB..");
			deletedCount = documentRepository.softDeleteByIds(activeIds, user);

			deletedDocs = activeOrNonDeletedDocs;

		} else if (request.getDeleteType() == DeleteType.HARD) {

			log.info("Hard delete initiated..");

			if (alreadySoftDeletedDocs.isEmpty()) {
				throw new IllegalArgumentException(DocumentGlobalConstants.DOCUMENT_SOFT_FIRST_BEFORE_HARD);
			}

			log.info("Audit trail initiared..");
			// Audit trail first
			for (Document document : alreadySoftDeletedDocs) {
				auditRepository.saveAudit(document.getDocumentId(), request.getDeleteType().name() + "_DELETE", user,
						request.getReason());
			}

			log.info("Hard delete from file system initiated..");
			// HARD delete → file system first
			List<Long> activeIds = alreadySoftDeletedDocs.stream().map(Document::getDocumentId).toList();
			for (Document document : alreadySoftDeletedDocs) {
				documentHelper.deleteFileFromLocal(document.getStoragePath(), storageBasePath);
			}

			log.info("Hard delete from DB initiated..");
			// HARD delete → db
			deletedCount = documentRepository.hardDeleteByIds(activeIds);

			deletedDocs = alreadySoftDeletedDocs;

			log.info("Hard delete fcompleted | user={} | deletedCount={}", user, deletedCount);
		} else {
			throw new IllegalArgumentException(DocumentGlobalConstants.INVALID_DELETETYPE);
		}

		return new DocumentDeleteResponse(deletedDocs.stream().map(Document::getDocumentId).toList(),
				request.getDeleteType(), deletedCount, user, LocalDateTime.now());
	}

	@Override
	public DocumentPagedResponse<DocumentListResponse> listDocuments(String user, DocumentListRequest request) {

		log.info("Listing documents | userId{} | page{} | size{}", user, request.getPage(), request.getSize());

		// essential for breaking large records into smaller, manageable chunks
		// (pages), which improves page loading performance and user experience
		int offset = request.getPage() * request.getSize();

		// fetch documents
		List<DocumentListResponse> documents = documentRepository.listDocuments(user, request.getFromDate(),
				request.getToDate(), request.getMinSize(), request.getMaxSize(), request.getSortBy(),
				request.getSortDir(), request.getSize(), offset);

		long total = documentRepository.countDocuments(user, request.getFromDate(), request.getToDate(),
				request.getMinSize(), request.getMaxSize());

		return DocumentPagedResponse.<DocumentListResponse>builder().content(documents).page(request.getPage())
				.size(request.getSize()).totalElements(total).build();
	}

	@Override
	public ResponseEntity<Resource> downloadDocument(Long documentId, String user) {

		log.debug("downloadDocument() starts | userId={} | documentId={} ", user, documentId);

		Document document = documentRepository.findDocumentById(documentId, user)
				.orElseThrow(() -> new EmptyResultDataAccessException("Document not found", 1));

		if (document.isFileDeleted()) {
			log.warn("Document is soft deleted | documentId={} ", documentId);
			throw new IllegalArgumentException(DocumentGlobalConstants.DOCUMENT_DELETED);
		}

		Path filePath = Paths.get(document.getStoragePath());
		if (!Files.exists(filePath)) {
			log.error("File not found on disk | path={}", filePath);
			throw new IllegalArgumentException(DocumentGlobalConstants.FILE_NOT_FOUND_ON_DISK);
		}

		Resource resource = new FileSystemResource(filePath);
		log.info("Document download successful | documentId={} | fileName={}", documentId, document.getFileName());

		// it will allow user to download the file on UI
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + document.getFileName() + "\"")
				.contentLength(document.getFileSize()).body(resource);
	}

}

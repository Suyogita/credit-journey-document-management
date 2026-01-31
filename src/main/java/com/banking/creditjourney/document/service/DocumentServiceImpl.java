package com.banking.creditjourney.document.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.dto.request.DeleteDocumentRequest;
import com.banking.creditjourney.document.dto.response.DeleteType;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.dto.response.DocumentPagedResponse;
import com.banking.creditjourney.document.dto.response.DocumentResponse;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;
import com.banking.creditjourney.document.repository.AuditRepository;
import com.banking.creditjourney.document.repository.DocumentRepository;

import jakarta.validation.constraints.Min;
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

		// Fetch documentsonly for logged-in user
		List<Document> documents = documentRepository.findByIds(documentIds, user);

		// Validate ownership & existence
		if (documents.isEmpty()) {
			throw new IllegalArgumentException(DocumentGlobalConstants.NO_DOCUMENT_FOUND);
		}

		if (documents.size() != documentIds.size()) {
			// Some Ids belong to other users or don't exist
			throw new IllegalArgumentException(DocumentGlobalConstants.DOCUMENT_NOT_BELONGS_TO_USER);
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
				documentHelper.deleteFileFromLocal(document.getStoragePath(), storageBasePath);
			}
			// HARD delete → db
			documentRepository.hardDeleteByIds(documentIds);
		}
	}

	@Override
	public DocumentPagedResponse<DocumentListResponse> listDocuments(String user, @Min(0) int page, @Min(1) int size,
			String sortBy, String sortDir, LocalDate fromDate, LocalDate toDate, Long minSize, Long maxSize) {

		log.info("Listing documents | userId{} | page{} | size{}", user, page, size);

		// essential for breaking large records into smaller, manageable chunks
		// (pages), which improves page loading performance and user experience
		int offset = page * size;

		// fetch documents
		List<DocumentListResponse> documents = documentRepository.listDocuments(user, fromDate, toDate, minSize,
				maxSize, sortBy, sortDir, size, offset);

		long total = documentRepository.countDocuments(user, fromDate, toDate, minSize, maxSize);

		return DocumentPagedResponse.<DocumentListResponse>builder().content(documents).page(page).size(size)
				.totalElements(total).build();
	}

	@Override
	public ResponseEntity<Resource> downloadDocument(Long documentId, String user) {

		Document document = documentRepository.findDocumentById(documentId, user);

		Path filePath = Paths.get(document.getStoragePath());
		if (!Files.exists(filePath)) {
			throw new IllegalArgumentException(DocumentGlobalConstants.FILE_NOT_FOUND_ON_DISK);
		}

		Resource resource = new FileSystemResource(filePath);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + document.getFileName() + "\"")
				.contentLength(document.getFileSize()).body(resource);
	}

}

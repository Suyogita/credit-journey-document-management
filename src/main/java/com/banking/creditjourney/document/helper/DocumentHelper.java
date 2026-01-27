package com.banking.creditjourney.document.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.DocumentRequest;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;

@Component
public class DocumentHelper {

	@Value("$document.storage.base_path")
	private String storageBasePath;

	public void validateFileBeforeUpload(MultipartFile file) {
		if (file.isEmpty()) {
			throw new RuntimeException(DocumentGlobalConstants.FILE_EMPTY);
		}

		if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
			throw new RuntimeException(DocumentGlobalConstants.ONLY_PDF_FILE);
		}

		if (file.getSize() > 5 * 1024 * 1024) {
			throw new RuntimeException(DocumentGlobalConstants.FILE_SIZE_5MB);
		}

	}

	// Checksum generation using message digest algorithm SHA-256
	public String generateChecksumForFile(MultipartFile file) {
		try {
			MessageDigest digest = MessageDigest.getInstance(DocumentGlobalConstants.SHA_256);
			byte[] hash = digest.digest(file.getBytes());
			return HexFormat.of().formatHex(hash);
		} catch (Exception e) {
			throw new RuntimeException(DocumentGlobalConstants.CHECKSUM_GENERATION_FAILED, e);
		}

	}

	public String storeFile(MultipartFile file, String userId) {
		try {

			Files.createDirectories(Path.of(storageBasePath));

			String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

			Path fileTarget = Path.of(storageBasePath, uniqueFileName);

			file.transferTo(fileTarget.toFile());

			return fileTarget.toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(DocumentGlobalConstants.FILE_STORAGE_FAILED, e);
		}
	}

	public Document prepareDocumentObject(String fileStoragePath, DocumentRequest request, MultipartFile file,
			String checkSumString) {
		Document document = new Document();
		document.setUserId(request.getUserId());
		document.setFileName(file.getOriginalFilename());
		document.setFileType(file.getContentType());
		document.setFileSize(file.getSize());
		document.setStoragePath(fileStoragePath);
		document.setChecksum(checkSumString);
		return document;

	}

}

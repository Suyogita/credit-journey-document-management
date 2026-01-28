package com.banking.creditjourney.document.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
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

	public Document prepareDocumentObject(String fileStoragePath, CreateDocumentRequest request, MultipartFile file,
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

	public void deleteFileFromLocal(String fileStoragePath) {
		if (fileStoragePath == null || fileStoragePath.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DocumentGlobalConstants.FILE_PATH_ERROR);
		}
		try {
			Path basePath = Paths.get(storageBasePath).toRealPath(); // configured path
			Path targetPath = Paths.get(fileStoragePath).toRealPath();

			// Prevents deleting outside storage folder
			if (!targetPath.startsWith(basePath)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, DocumentGlobalConstants.FILE_PATH_INVALID);
			}
			// File does not exist in path
			if (!Files.exists(targetPath)) {
				throw new IllegalStateException(DocumentGlobalConstants.FILE_NOT_FOUND_ON_DISK + targetPath);
			}
			// Safety check- must be file , not directory
			if (!Files.isRegularFile(targetPath)) {
				throw new IllegalStateException(DocumentGlobalConstants.NOT_REGULAR_FILE_ERROR + targetPath);
			}
			// delete file
			Files.delete(targetPath);

		} catch (IOException ex) {

			throw new RuntimeException(DocumentGlobalConstants.FILE_DELETE_FAILED + fileStoragePath, ex);
		}

	}

}

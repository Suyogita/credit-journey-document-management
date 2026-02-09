package com.banking.creditjourney.document.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocumentHelper {

	@Value("${document.storage.base.path}")
	private String storageBasePath;

	public void validateFileBeforeUpload(MultipartFile file) {
		if (file.isEmpty()) {
			throw new RuntimeException(DocumentGlobalConstants.FILE_EMPTY);
		}

		//MIME file validation Purpose: To prevent user from uploading virus or unwanted file- security check
		if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
			throw new RuntimeException(DocumentGlobalConstants.ONLY_PDF_FILE);
		}

		//Size validation. Maximum 5MB file can be uploaded
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

	public String storeFile(MultipartFile file) {
		try {

			Files.createDirectories(Path.of(storageBasePath));

			Path fileTarget = Path.of(storageBasePath, file.getOriginalFilename());

			file.transferTo(fileTarget.toFile());

			return fileTarget.toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(DocumentGlobalConstants.FILE_STORAGE_FAILED, e);
		}
	}

	public Document prepareDocumentObject(String fileStoragePath, MultipartFile file,
			String checkSumString, String user) {
		Document document = new Document();
		document.setUserId(user);
		document.setFileName(file.getOriginalFilename());
		document.setFileType(file.getContentType());
		document.setFileSize(file.getSize());
		document.setStoragePath(fileStoragePath);
		document.setChecksum(checkSumString);
		return document;

	}

	public void deleteFileFromLocal(String fileStoragePath, String storageBasePath) {

		if (fileStoragePath == null || fileStoragePath.isBlank()) {
			throw new IllegalStateException(DocumentGlobalConstants.FILE_PATH_ERROR);
		}

		if (storageBasePath == null || storageBasePath.isBlank()) {
			throw new IllegalStateException(DocumentGlobalConstants.FILE_PATH_ERROR);
		}
		try {
			Path basePath = Paths.get(storageBasePath).toAbsolutePath().normalize(); // configured path
			Path targetPath = Paths.get(fileStoragePath).toAbsolutePath().normalize();

			// Prevents deleting outside storage folder
			if (!targetPath.startsWith(basePath)) {
				throw new IllegalStateException(DocumentGlobalConstants.FILE_PATH_INVALID);
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
			log.info("File deleted successfully:{}", targetPath);

		} catch (IOException ex) {
			log.error("File deletion failed:{}", fileStoragePath, ex);
			throw new IllegalStateException(DocumentGlobalConstants.FILE_DELETE_FAILED + fileStoragePath, ex);
		}

	}

}

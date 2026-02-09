package com.banking.creditjourney.document.helper.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;

@ExtendWith(MockitoExtension.class)
class DocumentHelperTest {

	@InjectMocks
	private DocumentHelper helper;

	@TempDir
	Path tempDir;

	@Test
	void validateFile_validPdf() {
		MultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "data".getBytes());

		assertDoesNotThrow(() -> helper.validateFileBeforeUpload(file));
	}

	@Test
	void validateFile_empty_shouldFail() {
		MultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> helper.validateFileBeforeUpload(file));

		assertEquals(DocumentGlobalConstants.FILE_EMPTY, ex.getMessage());
	}

	@Test
	void validateFile_invalidMime_shouldFail() {
		MultipartFile file = new MockMultipartFile("file", "a.exe", "application/octet-stream", "data".getBytes());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> helper.validateFileBeforeUpload(file));

		assertEquals(DocumentGlobalConstants.ONLY_PDF_FILE, ex.getMessage());
	}

	@Test
	void generateChecksum_success() {
		MultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "data".getBytes());

		String checksum = helper.generateChecksumForFile(file);
		assertNotNull(checksum);
	}

	@Test
	void prepareDocumentObject_success() {
		Document doc = helper.prepareDocumentObject("/path",
				new MockMultipartFile("file", "a.pdf", "application/pdf", "data".getBytes()), "checksum", "user1");

		assertEquals("user1", doc.getUserId());
		assertEquals("a.pdf", doc.getFileName());
	}

	// delete

	// ------------------------------------------------
	// NULL / BLANK INPUTS
	// ------------------------------------------------

	@Test
	void deleteFile_nullFilePath_shouldThrowException() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(null, tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_PATH_ERROR));
	}

	@Test
	void deleteFile_blankFilePath_shouldThrowException() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(" ", tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_PATH_ERROR));
	}

	@Test
	void deleteFile_nullBasePath_shouldThrowException() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal("file.txt", null));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_PATH_ERROR));
	}

	@Test
	void deleteFile_blankBasePath_shouldThrowException() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal("file.txt", " "));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_PATH_ERROR));
	}

	// ------------------------------------------------
	// PATH TRAVERSAL PROTECTION
	// ------------------------------------------------

	@Test
	void deleteFile_outsideBaseDirectory_shouldThrowException() {
		Path outsideFile = tempDir.getParent().resolve("evil.txt");

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(outsideFile.toString(), tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_PATH_INVALID));
	}

	// ------------------------------------------------
	// FILE NOT FOUND
	// ------------------------------------------------

	@Test
	void deleteFile_fileDoesNotExist_shouldThrowException() {
		Path nonExistingFile = tempDir.resolve("missing.txt");

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(nonExistingFile.toString(), tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_NOT_FOUND_ON_DISK));
	}

	// ------------------------------------------------
	// DIRECTORY INSTEAD OF FILE
	// ------------------------------------------------

	@Test
	void deleteFile_isDirectory_shouldThrowException() throws IOException {
		Path directory = Files.createDirectory(tempDir.resolve("folder"));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(directory.toString(), tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.NOT_REGULAR_FILE_ERROR));
	}

	// ------------------------------------------------
	// SUCCESS CASE
	// ------------------------------------------------

	@Test
	void deleteFile_successfullyDeletesFile() throws IOException {
		Path file = Files.createFile(tempDir.resolve("test.pdf"));

		assertTrue(Files.exists(file));

		assertDoesNotThrow(() -> helper.deleteFileFromLocal(file.toString(), tempDir.toString()));

		assertFalse(Files.exists(file));
	}

	// ------------------------------------------------
	// IO FAILURE DURING DELETE
	// ------------------------------------------------

	@Test
	void deleteFile_ioException_shouldThrowWrappedException() throws IOException {
		Path file = Files.createFile(tempDir.resolve("locked.txt"));

		// Make file read-only (causes delete failure on most OS)
		file.toFile().setWritable(false);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> helper.deleteFileFromLocal(file.toString(), tempDir.toString()));

		assertTrue(ex.getMessage().contains(DocumentGlobalConstants.FILE_DELETE_FAILED));
	}

}

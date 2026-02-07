package com.banking.creditjourney.document.helper.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.request.CreateDocumentRequest;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;
import com.banking.creditjourney.document.helper.DocumentHelper;

@ExtendWith(MockitoExtension.class)
class DocumentHelperTest {

    @InjectMocks
    private DocumentHelper helper;

    @Test
    void validateFile_validPdf() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.pdf", "application/pdf", "data".getBytes()
        );

        assertDoesNotThrow(() -> helper.validateFileBeforeUpload(file));
    }

    @Test
    void validateFile_empty_shouldFail() {
        MultipartFile file = new MockMultipartFile(
                "file", "", "application/pdf", new byte[0]
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> helper.validateFileBeforeUpload(file));

        assertEquals(DocumentGlobalConstants.FILE_EMPTY, ex.getMessage());
    }

    @Test
    void validateFile_invalidMime_shouldFail() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.exe", "application/octet-stream", "data".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> helper.validateFileBeforeUpload(file));

        assertEquals(DocumentGlobalConstants.ONLY_PDF_FILE, ex.getMessage());
    }

    @Test
    void generateChecksum_success() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.pdf", "application/pdf", "data".getBytes()
        );

        String checksum = helper.generateChecksumForFile(file);
        assertNotNull(checksum);
    }

    @Test
    void prepareDocumentObject_success() {
        Document doc = helper.prepareDocumentObject(
                "/path",
                new CreateDocumentRequest(),
                new MockMultipartFile("file", "a.pdf", "application/pdf", "data".getBytes()),
                "checksum",
                "user1"
        );

        assertEquals("user1", doc.getUserId());
        assertEquals("a.pdf", doc.getFileName());
    }
}


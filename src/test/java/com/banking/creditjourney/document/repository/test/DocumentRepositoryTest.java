package com.banking.creditjourney.document.repository.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.repository.DocumentRepository;
import com.banking.creditjourney.document.repository.DocumentsQueries;

@ExtendWith(MockitoExtension.class)
class DocumentRepositoryTest {

	@Mock
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@InjectMocks
	private DocumentRepository documentRepository;

	@Test
	void saveDocument_success() {

		Document doc = new Document();
		doc.setUserId("user1");
		doc.setFileName("test.pdf");
		doc.setFileType("application/pdf");
		doc.setFileSize(1024L);
		doc.setChecksum("abc123checksumtest");
		doc.setStoragePath("/tmp/test.pdf");

		when(namedParameterJdbcTemplate.update(eq(DocumentsQueries.INSERT_DOCUMENT), any(MapSqlParameterSource.class),
				any(KeyHolder.class), any(String[].class))).thenAnswer(invocation -> {
					KeyHolder kh = invocation.getArgument(2);
					((GeneratedKeyHolder) kh).getKeyList().add(Map.of("documentid", 10L));
					return 1;
				});

		Long documentId = documentRepository.saveDocumentIntoDB(doc);

		assertEquals(10L, documentId);

		verify(namedParameterJdbcTemplate, times(1)).update(eq(DocumentsQueries.INSERT_DOCUMENT),
				any(MapSqlParameterSource.class), any(KeyHolder.class), any(String[].class));

	}

	@Test
	void findByChecksum_present() {
		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
				.thenReturn(List.of(new Document()));

		Optional<Document> doc = documentRepository.findByCheckSum("checksum");
		assertTrue(doc.isPresent());
	}

	@Test
	void findByChecksum_empty() {
		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
				.thenReturn(List.of());

		Optional<Document> doc = documentRepository.findByCheckSum("checksum");
		assertTrue(doc.isEmpty());
	}
}

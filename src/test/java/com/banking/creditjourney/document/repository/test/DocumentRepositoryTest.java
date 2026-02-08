package com.banking.creditjourney.document.repository.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
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

	@Test
	void findByIds_success() {
		when(namedParameterJdbcTemplate.query(eq(DocumentsQueries.FIND_BY_IDS), any(MapSqlParameterSource.class),
				any(RowMapper.class))).thenReturn(List.of(new Document(), new Document()));

		List<Document> documents = documentRepository.findByIds(List.of(1L, 2L), "user1");

		assertEquals(2, documents.size());
	}

	@Test
	void findByIds_empty() {
		when(namedParameterJdbcTemplate.query(eq(DocumentsQueries.FIND_BY_IDS), any(MapSqlParameterSource.class),
				any(RowMapper.class))).thenReturn(List.of());

		List<Document> documents = documentRepository.findByIds(List.of(99L), "user1");

		assertTrue(documents.isEmpty());
	}

	@Test
	void softDeleteByIds_success() {
		when(namedParameterJdbcTemplate.update(eq(DocumentsQueries.SOFT_DELETE_BY_IDS),
				any(MapSqlParameterSource.class))).thenReturn(2);

		int count = documentRepository.softDeleteByIds(List.of(1L, 2L), "user1");

		assertEquals(2, count);
	}

	@Test
	void softDeleteByIds_zeroUpdated() {
		when(namedParameterJdbcTemplate.update(eq(DocumentsQueries.SOFT_DELETE_BY_IDS),
				any(MapSqlParameterSource.class))).thenReturn(0);

		int count = documentRepository.softDeleteByIds(List.of(99L), "user1");

		assertEquals(0, count);
	}

	@Test
	void hardDeleteByIds_success() {
		when(namedParameterJdbcTemplate.update(eq(DocumentsQueries.HARD_DELETE_BY_IDS),
				any(MapSqlParameterSource.class))).thenReturn(1);

		int count = documentRepository.hardDeleteByIds(List.of(1L));

		assertEquals(1, count);
	}

	@Test
	void hardDeleteByIds_zeroDeleted() {
		when(namedParameterJdbcTemplate.update(eq(DocumentsQueries.HARD_DELETE_BY_IDS),
				any(MapSqlParameterSource.class))).thenReturn(0);

		int count = documentRepository.hardDeleteByIds(List.of(99L));

		assertEquals(0, count);
	}

	// listing document

	@Test
	void listDocuments_success_shouldQueryDb() {
		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
				any(BeanPropertyRowMapper.class))).thenReturn(List.of(new DocumentListResponse()));

		List<DocumentListResponse> result = documentRepository.listDocuments("user123", any(LocalDate.class),
				any(LocalDate.class), any(Long.class), any(Long.class), anyString(), anyString(), anyInt(), anyInt());

		assertFalse(result.isEmpty());
	}

	@Test
	void countDocuments_success_shouldReturnCount() {
		when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
				.thenReturn(5L);

		Long count = documentRepository.countDocuments("user123", any(LocalDate.class), any(LocalDate.class),
				any(Long.class), any(Long.class));

		assertEquals(5L, count);
	}

	@Test
	void listDocuments_invalidSort_shouldThrowException() {
		assertThrows(IllegalArgumentException.class, () -> documentRepository.listDocuments("user123",
				any(LocalDate.class), any(LocalDate.class), any(Long.class), any(Long.class), "hack", "ASC", 0, 10));
	}

	@Test
	void listDocuments_success_noFilters() {

		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
				any(BeanPropertyRowMapper.class))).thenReturn(List.of(new DocumentListResponse()));

		List<DocumentListResponse> result = documentRepository.listDocuments("user123", null, null, null, null,
				"created_at", "ASC", 10, 0);

		assertEquals(1, result.size());
	}

	@Test
	void listDocuments_success_withDateRange() {

		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
				any(BeanPropertyRowMapper.class))).thenReturn(List.of(new DocumentListResponse()));

		List<DocumentListResponse> result = documentRepository.listDocuments("user123", LocalDate.now().minusDays(10),
				LocalDate.now(), null, null, "created_at", "DESC", 10, 0);

		assertFalse(result.isEmpty());
	}

	@Test
	void listDocuments_success_withSizeFilters() {

		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
				any(BeanPropertyRowMapper.class))).thenReturn(List.of(new DocumentListResponse()));

		List<DocumentListResponse> result = documentRepository.listDocuments("user123", null, null, 100L, 10_000L,
				"file_size", "ASC", 10, 0);

		assertEquals(1, result.size());
	}

	@Test
	void listDocuments_emptyResult() {

		when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
				any(BeanPropertyRowMapper.class))).thenReturn(List.of());

		List<DocumentListResponse> result = documentRepository.listDocuments("user123", null, null, null, null,
				"created_at", "ASC", 10, 0);

		assertTrue(result.isEmpty());
	}

	@Test
	void listDocuments_invalidSort_shouldThrowExceptions() {
		assertThrows(IllegalArgumentException.class,
				() -> documentRepository.listDocuments("user123", null, null, null, null, "hack_column", "ASC", 10, 0));
	}

	/*
	 * --------------------------------------------------- countDocuments tests
	 * ---------------------------------------------------
	 */

	@Test
	void countDocuments_success() {

		when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
				.thenReturn(5L);

		Long count = documentRepository.countDocuments("user123", null, null, null, null);

		assertEquals(5L, count);
	}

	@Test
	void countDocuments_zeroResult() {

		when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
				.thenReturn(0L);

		Long count = documentRepository.countDocuments("user123", null, null, null, null);

		assertEquals(0L, count);
	}

}

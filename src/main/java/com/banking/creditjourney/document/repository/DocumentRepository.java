package com.banking.creditjourney.document.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.banking.creditjourney.document.domain.model.Document;
import com.banking.creditjourney.document.dto.response.DocumentListResponse;
import com.banking.creditjourney.document.global.constant.DocumentGlobalConstants;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class DocumentRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public DocumentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		super();
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	// Whitelisted sortable columns(SECURITY)
	private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("created_at", "file_size", "file_name",
			"updated_at");

	public Long saveDocumentIntoDB(Document doc) {
		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DocumentGlobalConstants.USERID, doc.getUserId())
				.addValue("fileName", doc.getFileName()).addValue("fileType", doc.getFileType())
				.addValue("fileSize", doc.getFileSize()).addValue("checksum", doc.getChecksum())
				.addValue("storagePath", doc.getStoragePath());
		KeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(DocumentsQueries.INSERT_DOCUMENT, params, keyHolder,
				new String[] { "documentid" });

		// Converts int value to Long and return
		return keyHolder.getKey().longValue();
	}

	public Optional<Document> findByCheckSum(String checkSumString) {
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("checksum", checkSumString);
		List<Document> documents = namedParameterJdbcTemplate.query(DocumentsQueries.FIND_BY_CHECKSUM, params,
				new BeanPropertyRowMapper<>(Document.class));
		return documents.stream().findFirst();
	}

	// find document(s) by ids
	public List<Document> findByIds(List<Long> documentIds, String user) {

		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DocumentGlobalConstants.DOCUMENTIDS, documentIds)
				.addValue(DocumentGlobalConstants.USERID, user);

		return namedParameterJdbcTemplate.query(DocumentsQueries.FIND_BY_IDS, params,
				new BeanPropertyRowMapper<>(Document.class));
	}

	// soft delete
	public int softDeleteByIds(List<Long> documentIds, String deletedBy) {

		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DocumentGlobalConstants.DOCUMENTIDS, documentIds)
				.addValue(DocumentGlobalConstants.USERID, deletedBy).addValue("deletedBy", deletedBy);

		return namedParameterJdbcTemplate.update(DocumentsQueries.SOFT_DELETE_BY_IDS, params);
	}

	// hard delete
	public int hardDeleteByIds(List<Long> documentIds) {

		MapSqlParameterSource params = new MapSqlParameterSource(DocumentGlobalConstants.DOCUMENTIDS, documentIds);

		return namedParameterJdbcTemplate.update(DocumentsQueries.HARD_DELETE_BY_IDS, params);
	}

	public List<DocumentListResponse> listDocuments(String user, LocalDate fromDate, LocalDate toDate, Long minSize,
			Long maxSize, String sortBy, String sortDir, @Min(1) int size, int offset) {

		validateSort(sortBy, sortDir);

		String sql = String.format(DocumentsQueries.LIST_DOCUMENTS, sortBy, sortDir);

		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DocumentGlobalConstants.USERID, user)
				.addValue("fromDate", fromDate).addValue("toDate", toDate).addValue("minSize", minSize)
				.addValue("maxSize", maxSize);
		params.addValue("limit", size);
		params.addValue("offset", offset);

		log.debug("Executing list documents query");

		return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(DocumentListResponse.class));
	}

	private void validateSort(String sortBy, String sortDir) {
		if (!ALLOWED_SORT_COLUMNS.contains(sortBy)) {
			throw new IllegalArgumentException(DocumentGlobalConstants.INVALID_SORTBY + sortBy);
		}
		if (!"ASC".equalsIgnoreCase(sortDir) && !"DESC".equalsIgnoreCase(sortDir)) {
			throw new IllegalArgumentException(DocumentGlobalConstants.INVALID_SORTDIR + sortDir);
		}

	}

	public long countDocuments(String user, LocalDate fromDate, LocalDate toDate, Long minSize, Long maxSize) {
		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DocumentGlobalConstants.USERID, user)
				.addValue("fromDate", fromDate).addValue("toDate", toDate).addValue("minSize", minSize)
				.addValue("maxSize", maxSize);

		return namedParameterJdbcTemplate.queryForObject(DocumentsQueries.COUNT_DOCUMENTS, params, Long.class);
	}

	public Optional<Document> findDocumentById(Long documentId, String user) {

		log.info("findDocumentById() starts ");

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("documentId", documentId).addValue(DocumentGlobalConstants.USERID,
				user);

		return namedParameterJdbcTemplate
				.query(DocumentsQueries.FIND_BY_ID, params, BeanPropertyRowMapper.newInstance(Document.class)).stream()
				.findFirst();

	}

}

package com.banking.creditjourney.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.banking.creditjourney.document.domain.model.Document;

@Repository
public class DocumentRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public DocumentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		super();
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public Long saveDocumentIntoDB(Document doc) {
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", doc.getUserId())
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
	public List<Document> findByIds(List<Long> documentIds) {

		MapSqlParameterSource params = new MapSqlParameterSource("documentIds", documentIds);

		return namedParameterJdbcTemplate.query(DocumentsQueries.FIND_BY_IDS, params,
				new BeanPropertyRowMapper<>(Document.class));
	}

	// soft delete
	public int softDeleteByIds(List<Long> documentIds, String deletedBy) {

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("documentIds", documentIds)
				.addValue("deletedBy", deletedBy);

		return namedParameterJdbcTemplate.update(DocumentsQueries.SOFT_DELETE_BY_IDS, params);
	}

	// hard delete
	public int hardDeleteByIds(List<Long> documentIds) {

		MapSqlParameterSource params = new MapSqlParameterSource("documentIds", documentIds);

		return namedParameterJdbcTemplate.update(DocumentsQueries.HARD_DELETE_BY_IDS, params);
	}

}

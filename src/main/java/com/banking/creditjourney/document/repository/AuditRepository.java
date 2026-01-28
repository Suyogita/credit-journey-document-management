package com.banking.creditjourney.document.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuditRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public void saveAudit(Long documentId, String action, String performedBy, String reason) {

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("documentId", documentId)
				.addValue("action", action).addValue("performedBy", performedBy).addValue("reason", reason);

		namedParameterJdbcTemplate.update(DocumentsQueries.INSERT_AUDIT, params);
	}
}

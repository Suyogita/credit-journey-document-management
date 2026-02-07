package com.banking.creditjourney.document.repository.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.banking.creditjourney.document.repository.AuditRepository;

@ExtendWith(MockitoExtension.class)
class AuditRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private AuditRepository auditRepository;

    @Test
    void saveAudit_success() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(1);

        assertDoesNotThrow(() ->
                auditRepository.saveAudit(1L, "UPLOAD", "user1", "SUCCESS")
        );
    }

    @Test
    void saveAudit_failure() {
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> auditRepository.saveAudit(1L, "UPLOAD", "user1", "FAIL")
        );
    }
}

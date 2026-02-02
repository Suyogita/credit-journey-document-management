package com.banking.creditjourney.document.repository;

public final class DocumentsQueries {

	public DocumentsQueries() {
		super();
	}

	public static final String INSERT_DOCUMENT = """
			INSERT INTO documents(user_id,file_name,file_type,file_size,checksum,storage_path)
			VALUES (:userId, :fileName, :fileType, :fileSize, :checksum, :storagePath)
			""";

	public static final String FIND_BY_CHECKSUM = """
			SELECT * FROM documents WHERE checksum= :checksum
			""";

	public static final String FIND_BY_IDS = """
			SELECT
			 documentid,
			 user_id,
			 file_name,
			 file_type,
			 file_size,
			 checksum,
			 storage_path,
			 is_deleted AS file_deleted,created_at,
			 updated_at FROM documents
			WHERE documentid IN (:documentIds)
			AND user_id = :userId
						""";

	// soft delete
	public static final String SOFT_DELETE_BY_IDS = """
			UPDATE documents
			SET is_deleted = true,
			    deleted_at = CURRENT_TIMESTAMP,
			    deleted_by = :deletedBy,
			    user_id= :deletedBy
			WHERE documentid IN (:documentIds)
			""";

	// hard delete
	public static final String HARD_DELETE_BY_IDS = """
			DELETE FROM documents
			WHERE documentid IN (:documentIds)
			""";

	// delete audit trail
	public static final String INSERT_AUDIT = """
			INSERT INTO document_audit(documentid,action,performed_by,reason)
			VALUES (:documentId, :action, :performedBy, :reason)
			""";

	public static final String LIST_DOCUMENTS = """
			SELECT documentid AS documentId,
			       file_name AS fileName,
			       file_type AS fileType,
			       file_size AS fileSize,
			       created_at AS createdAt,
			       updated_at AS updatedAt
			FROM documents
			WHERE user_id = :userId
			  AND is_deleted = FALSE
			  AND (:fromDate IS NULL OR created_at >= :fromDate)
			  AND (:toDate IS NULL OR created_at <= :toDate)
			  AND (:minSize IS NULL OR file_size >= :minSize)
			  AND (:maxSize IS NULL OR file_size <= :maxSize)
			ORDER BY %s %s
			LIMIT :limit OFFSET :offset
			""";

	public static final String COUNT_DOCUMENTS = """
			SELECT COUNT(1)
			FROM documents
			WHERE user_id = :userId
			  AND is_deleted = FALSE
			  AND (:fromDate IS NULL OR created_at >= :fromDate)
			  AND (:toDate IS NULL OR created_at <= :toDate)
			  AND (:minSize IS NULL OR file_size >= :minSize)
			  AND (:maxSize IS NULL OR file_size <= :maxSize)
			""";

	public static final String FIND_BY_ID = """
			SELECT *
			FROM documents
			WHERE documentid = :documentId
			  AND user_id = :userId
			  AND is_deleted = FALSE
			""";

}

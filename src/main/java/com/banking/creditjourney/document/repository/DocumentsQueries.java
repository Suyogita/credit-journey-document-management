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
			SELECT * FROM documents
			WHERE documentid IN (:documentIds)
			""";

	// soft delete
	public static final String SOFT_DELETE_BY_IDS = """
			UPDATE documents
			SET is_deleted = true,
			    deleted_at = CURRENT_TIMESTAMP,
			    deleted_by = :deletedBy
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
}

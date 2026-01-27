package com.banking.creditjourney.document.repository;

public final class DocumentsQueries {

	public DocumentsQueries() {
		super();
	}

	public static final String INSERT_DOCUMENT = """
			INSERT INTO documents(user_id,file_name,file_type,file_size,checksum,storage_path)
			VALUES ()
			""";

	public static final String FIND_BY_CHECKSUM = """
			SELECT * FROM documents WHERE checksum= :checksum
			""";
	
}

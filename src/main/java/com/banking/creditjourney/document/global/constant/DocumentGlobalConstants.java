package com.banking.creditjourney.document.global.constant;

public class DocumentGlobalConstants {
	public static final String FILE_EMPTY = "File is empty";
	public static final String ONLY_PDF_FILE = "Only PDF file is allowed";
	public static final String FILE_SIZE_5MB = "File size exceeds 5MB";
	public static final String SHA_256 = "SHA-256";
	public static final String CHECKSUM_GENERATION_FAILED = "Failed to generate checksum";
	public static final String BASE_UPLOAD_DIRECTORY = "uploads/";
	public static final String FILE_STORAGE_FAILED = "File storage failed";
	public static final String DUPLICATE_FILE = "Duplicate file detected";
	public static final String FILE_UPLOAD_SUCCESS = "File uploaded successfully";
	public static final String NO_DOCUMENT_FOUND = "No document(s) found for deletion";
	public static final String FILE_PATH_ERROR = "File path is null or empty. Skipping deletion";
	public static final String FILE_DELETE_FAILED = "File deletion failed for path: ";
	public static final String FILE_NOT_FOUND_ON_DISK = "File not found on disk: ";
	public static final String NOT_REGULAR_FILE_ERROR = "Path is not a regular file: ";
	public static final String FILE_PATH_INVALID = "File path outside allowed storage directory";
	public static final String DOCUMENT_NOT_BELONGS_TO_USER = "One or more document(s) do not belong to the user or are already deleted";
	public static final String INVALID_SORTBY = "Invalid sortBy field: ";
	public static final String INVALID_SORTDIR = "Invalid sortDir: ";
	public static final String INVALID_DELETETYPE = "Invalid deleteType. Allowed exact values: SOFT,HARD";
	public static final String DOCUMENT_DELETED_ALREADY = "All requested documents are SOFT deleted already";
	public static final String DOCUMENT_SOFT_FIRST_BEFORE_HARD = "Document must be soft deleted before hard delete";
}
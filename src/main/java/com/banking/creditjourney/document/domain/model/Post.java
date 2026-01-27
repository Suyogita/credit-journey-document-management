package com.banking.creditjourney.document.domain.model;

public class Post {
	private Long postId;
	private Long documentId;
	private String title;

	public Post() {
		super();
	}

	public Post(Long postId, Long documentId, String title) {
		super();
		this.postId = postId;
		this.documentId = documentId;
		this.title = title;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}

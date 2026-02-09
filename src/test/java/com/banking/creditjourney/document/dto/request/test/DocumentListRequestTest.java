package com.banking.creditjourney.document.dto.request.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.banking.creditjourney.document.dto.request.DocumentListRequest;

class DocumentListRequestTest {

	@Test
	void builder_buildSuccessfully() {
		DocumentListRequest request = DocumentListRequest.builder().page(0).size(0).build();

		assertEquals(0, request.getPage());
		assertEquals(0, request.getSize());

	}

}

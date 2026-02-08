package com.banking.creditjourney.document.dto.request.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.banking.creditjourney.document.dto.request.DocumentListRequest;

public class DocumentListRequestTest {

	@Test
	void builder_buildSuccessfully() {
		DocumentListRequest request = DocumentListRequest.builder().page(0).size(0).build();

		assertEquals(1, request.getPage());

	}

}

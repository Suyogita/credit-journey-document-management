package com.banking.creditjourney.document;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DocumentManagementApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainMethodRunsSuccessfully() {
		assertDoesNotThrow(() -> DocumentManagementApplication.main(new String[] {}));
	}

}

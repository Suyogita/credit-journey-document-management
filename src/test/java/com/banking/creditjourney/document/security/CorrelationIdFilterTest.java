
package com.banking.creditjourney.document.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@Mock
	private FilterChain filterChain;

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Test
	void shouldSetAndClearCorrelationId() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		doAnswer(invocation -> {
			String correlationId = MDC.get("correlationId");
			assertNotNull(correlationId);
			assertFalse(correlationId.isBlank());
			return null;
		}).when(filterChain).doFilter(any(), any());

		filter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
		assertNull(MDC.get("correlationId"));
	}

	@Test
	void shouldClearMdcEvenWhenExceptionOccurs() throws Exception{

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		doThrow(new ServletException("boom")).when(filterChain).doFilter(any(), any());

		assertThrows(ServletException.class, () -> filter.doFilterInternal(request, response, filterChain));

		assertNull(MDC.get("correlationId"));
	}
}

package com.banking.creditjourney.document.exception.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.banking.creditjourney.document.exception.SecurityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class SecurityExceptionHandlerTest {
	private SecurityExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new SecurityExceptionHandler();
	}

	@Test
	void shouldReturn401WhenAuthenticationFails() throws Exception {

		HttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AuthenticationException authException = new AuthenticationException("Unauthorized") {
		};

		handler.commence(request, response, authException);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		assertEquals("application/json", response.getContentType());

		String body = response.getContentAsString();
		assertTrue(body.contains("UNAUTHORIZED"));
		assertTrue(body.contains("You are not authorized"));
	}

	@Test
	void shouldReturn403WhenAccessIsDenied() throws Exception {

		HttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AccessDeniedException accessDeniedException = new AccessDeniedException("Forbidden");

		handler.handle(request, response, accessDeniedException);

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertEquals("application/json", response.getContentType());

		String body = response.getContentAsString();
		assertTrue(body.contains("FORBIDDEN"));
		assertTrue(body.contains("Access denied"));

	}
}

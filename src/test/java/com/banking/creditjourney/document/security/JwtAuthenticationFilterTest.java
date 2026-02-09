
package com.banking.creditjourney.document.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter filter;
	private FilterChain filterChain;
	private HttpServletResponse response;

	@BeforeEach
	void setUp() {
		filter = new JwtAuthenticationFilter();
		filterChain = mock(FilterChain.class);
		response = mock(HttpServletResponse.class);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	// ✅ 1. Header present → authentication should be set
	@Test
	void shouldSetAuthenticationWhenUserHeaderPresent() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(JwtAuthenticationFilter.USER_HEADER, "user1");

		filter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		verify(filterChain).doFilter(request, response);
	}

	// ✅ 2. Header missing → authentication should NOT be set
	@Test
	void shouldNotSetAuthenticationWhenHeaderMissing() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest();

		filter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	// ✅ 3. Authentication already exists → should not override
	@Test
	void shouldNotOverrideExistingAuthentication() throws ServletException, IOException {

		UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken("existingUser",
				null);

		SecurityContextHolder.getContext().setAuthentication(existingAuth);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(JwtAuthenticationFilter.USER_HEADER, "newUser");

		filter.doFilterInternal(request, response, filterChain);

		assertEquals("existingUser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		verify(filterChain).doFilter(request, response);
	}

	// ✅ 4. Filter chain must always continue
	@Test
	void shouldAlwaysContinueFilterChain() throws ServletException, IOException {

		MockHttpServletRequest request = new MockHttpServletRequest();

		filter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
	}
}

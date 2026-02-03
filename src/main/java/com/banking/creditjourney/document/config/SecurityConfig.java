package com.banking.creditjourney.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.banking.creditjourney.document.exception.SecurityExceptionHandler;
import com.banking.creditjourney.document.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final SecurityExceptionHandler securityExceptionHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.httpBasic(httpBasic -> httpBasic.disable()).formLogin(form -> form.disable())

				.csrf(csrf -> csrf.disable())
				.exceptionHandling(ex -> ex.authenticationEntryPoint(securityExceptionHandler)
						.accessDeniedHandler(securityExceptionHandler))

				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers("/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**",
										"/swagger-ui.html", "/api/documentmgmt/**")
								.permitAll().anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.headers(headers -> headers.frameOptions(frame -> frame.disable()))
				// .httpBasic(Customizer.withDefaults())
				.build();

	}

}
package com.banking.creditjourney.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	// Currently all requests are allowed-temporary config
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.
		// Disable CSRF (required for H2 and API's)
				csrf(csrf -> csrf.disable())
				// Allow H2 console to be rendered in frames
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				// Allow everything as of now
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

}

//Backup config post JWT implementation

/*
 * public class SecurityConfig { // Currently all requests are allowed-temporary
 * config
 * 
 * @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)
 * throws Exception { http. // Disable CSRF (required for H2 and API's)
 * csrf(csrf -> csrf.disable()) // Allow H2 console to be rendered in frames
 * .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) //
 * Allow everything as of now .authorizeHttpRequests(auth -> auth
 * .requestMatchers("/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**",
 * "/swagger-ui.html", "/api/documentmgmt/**") .permitAll()
 * 
 * .anyRequest().authenticated());
 * 
 * // .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); return
 * http.build(); }
 * 
 * }
 * 
 */

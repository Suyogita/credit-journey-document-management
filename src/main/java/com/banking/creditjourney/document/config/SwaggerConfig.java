package com.banking.creditjourney.document.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI api() {
		final String securitySchemeName = "Authorization";
		return new OpenAPI().info(new Info()
				.title("Credit Journey-Document Management API")
				.description("REST API's managging document upload, retrieval, listing and deletion").version("V1"));

		/*
		 *Week 2:
		 * .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
		 * .components(new
		 * io.swagger.v3.oas.models.Components().addSecuritySchemes(securitySchemeName,
		 * new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).
		 * scheme("bearer") .bearerFormat("JWT")));
		 */
	}
}

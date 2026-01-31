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

		return new OpenAPI()
				.info(new Info().title("Credit Journey-Document Management API")
						.description("REST API's managging document upload, retrieval, listing and deletion")
						.version("V1"))
				.addSecurityItem(new SecurityRequirement().addList("JWT"))
				.components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("JWT", new SecurityScheme()
						.type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-USER-ID")));

	}
}

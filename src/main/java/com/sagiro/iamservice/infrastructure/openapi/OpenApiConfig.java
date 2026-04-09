package com.sagiro.iamservice.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI iamOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("IAM Service API")
                        .description("""
                                IAM service for a thesis-oriented international remittances platform.
                                Keycloak acts as the external identity provider and token issuer, while this service
                                owns the local access context, user state, and the summarized verification projection.
                                """)
                        .version("v1")
                        .contact(new Contact().name("Techi Thesis Backend")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

package com.expense.tracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) metadata.
 * A "Bearer" security scheme is registered so you can test secured
 * endpoints directly from Swagger UI using the Authorize button.
 */
@Configuration
public class AppConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Expense Tracker API")
                        .description("""
                                REST API for a personal expense tracker.

                                Try it: register an account via /api/auth/register, login via /api/auth/login,
                                then copy the returned token and click the green Authorize button (Paste it as a raw
                                Bearer token). All transaction/category/dashboard/report endpoints are protected.
                                """)
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes(
                        BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
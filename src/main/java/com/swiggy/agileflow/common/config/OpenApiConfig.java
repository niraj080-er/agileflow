package com.swiggy.agileflow.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger metadata. The interactive docs are served by springdoc at
 * /swagger-ui.html with the raw spec at /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agileFlowOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("AgileFlow API")
            .description("Backend foundation for the AgileFlow project management platform.")
            .version("v0.0.1"));
    }
}

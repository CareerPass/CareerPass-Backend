// Copilot: create a Spring @Configuration class OpenApiConfig that sets up OpenAPI info
// with title "CareerPass API", version "v1", and description "User 관련 API 문서"
package com.careerpass.global.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerPass API")
                        .version("v1")
                        .description("User 관련 API 문서"));
    }
}
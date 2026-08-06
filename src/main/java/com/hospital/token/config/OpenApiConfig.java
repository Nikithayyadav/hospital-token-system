package com.hospital.token.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalTokenOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Yashoda Hospitals - Patient Token & Queue Management API")
                                .description(
                                        "REST API documentation for patient registration, " +
                                                "doctor availability, token generation and hospital queue management."
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Yashoda Hospitals")
                                )
                );
    }
}
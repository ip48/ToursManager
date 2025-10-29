package com.innatour.toursmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI toursManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tours Manager API")
                        .description("REST API for Tours Manager - Web and Mobile clients")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tours Manager Team")
                                .email("contact@toursmanager.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.toursmanager.com")
                                .description("Production Server (future)")
                ));
    }
}

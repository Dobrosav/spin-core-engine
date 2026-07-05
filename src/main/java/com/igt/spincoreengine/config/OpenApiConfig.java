package com.igt.spincoreengine.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Spin Core Engine API",
                version = "1.0",
                description = "REST API for Spin Core Engine including player balance, deposit and spin functionalities.",
                contact = @Contact(
                        name = "IGT Support",
                        email = "support@igt.com",
                        url = "https://www.igt.com"
                ),
                license = @License(
                        name = "API License",
                        url = "https://www.igt.com/license"
                )
        ),
        servers = {
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:11150"
                )
        }
)
public class OpenApiConfig {
}

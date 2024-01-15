package com.oha.posting.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;


@OpenAPIDefinition(
        info = @Info(title = "OHA",
                description = "API명세",
                version = "0.0.1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

}

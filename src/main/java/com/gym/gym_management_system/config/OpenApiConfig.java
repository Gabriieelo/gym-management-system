package com.gym.gym_management_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SEGURIDAD_BEARER = "bearerAuth";

    @Bean
    public OpenAPI gymOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym Management System API")
                        .version("1.0")
                        .description("API para clientes, pagos, caja, asistencias y usuarios"))
                .addSecurityItem(new SecurityRequirement().addList(SEGURIDAD_BEARER))
                .components(new Components().addSecuritySchemes(
                        SEGURIDAD_BEARER,
                        new SecurityScheme()
                                .name(SEGURIDAD_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}

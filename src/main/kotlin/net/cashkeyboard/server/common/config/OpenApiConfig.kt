package net.cashkeyboard.server.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-token",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }

    private fun apiInfo() = Info()
        .title("Cashkeyboard API")
        .description("Cashkeyboard API")
        .version("v1.0")
        .contact(
            Contact()
                .name("API Support")
                .email("support@cashkeyboard.net")
        )
        .license(
            License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")
        )
}
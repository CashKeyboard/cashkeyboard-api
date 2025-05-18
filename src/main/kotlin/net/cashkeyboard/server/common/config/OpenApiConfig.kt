package net.cashkeyboard.server.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    companion object {
        private const val SECURITY_SCHEME_NAME = "Bearer Authentication"
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .components(
                Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList(SECURITY_SCHEME_NAME)
            )
    }

    private fun apiInfo() = Info()
        .title("Cashkeyboard API")
        .description("""
            Cashkeyboard API 문서
            
            ## 인증 방법
            1. `/api/v1/auth/login`으로 로그인하여 JWT 토큰을 획득합니다.
            2. 우측 상단의 🔒 Authorize 버튼을 클릭합니다.
            3. Bearer 토큰 필드에 `eyJ...` 형태의 토큰을 입력합니다. (Bearer 접두사 제외)
            4. Authorize 버튼을 클릭하면 모든 요청에 자동으로 헤더가 추가됩니다.
        """.trimIndent())
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
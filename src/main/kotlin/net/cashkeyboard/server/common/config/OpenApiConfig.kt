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

    companion object {
        const val BEARER_AUTH_SCHEME = "Bearer Authentication"
        const val ADMIN_KEY_SCHEME = "Admin Key"
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .components(
                Components()
                    // JWT Bearer Token Authentication
                    .addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT token for user authentication")
                    )
                    // Admin API Key Authentication
                    .addSecuritySchemes(
                        ADMIN_KEY_SCHEME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name("X-Admin-Key")
                            .description("Admin secret key for administrative operations")
                    )
            )
        // Remove global security requirement - let individual endpoints specify their requirements
    }

    private fun apiInfo() = Info()
        .title("Cashkeyboard API")
        .description("""
            Cashkeyboard API Documentation
            
            ## Authentication Methods
            
            ### 1. User Authentication (JWT)
            - **How to get token**: Call `/api/v1/auth/login` with your credentials
            - **How to use**: Click 🔒 "Bearer Authentication" below and enter your JWT token
            - **Format**: Just paste the token (without "Bearer " prefix)
            - **Usage**: Required for user endpoints like product browsing and user management
            
            ### 2. Admin Authentication (API Key)  
            - **How to get key**: Contact system administrator for admin secret key
            - **How to use**: Click 🔒 "Admin Key" below and enter the admin secret key
            - **Format**: Enter the exact secret key string
            - **Usage**: Required for admin endpoints like product management
            
            ## API Structure
            - `/api/v1/auth/login` - User login (public)
            - `/api/v1/users` - User registration (public)
            - `/api/v1/users/**` - User management (requires JWT)
            - `/api/v1/products` - User product browsing (requires JWT)
            - `/api/v1/admin/products/**` - Admin product management (requires Admin Key)
            
            ## How to Test APIs
            1. **Register/Login**: First create a user or login to get JWT token
            2. **Authorize**: Use the 🔒 buttons above to enter your credentials
            3. **Test**: Try the protected endpoints - they should now work!
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
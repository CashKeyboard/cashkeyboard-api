package net.cashkeyboard.server.auth.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.cashkeyboard.server.auth.api.dto.LoginRequest
import net.cashkeyboard.server.auth.api.dto.LoginResponse
import net.cashkeyboard.server.auth.application.command.LoginCommand
import net.cashkeyboard.server.auth.application.command.LoginCommandHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "User authentication API")
class AuthControllerV1(
    private val loginCommandHandler: LoginCommandHandler
) {

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = """
            Login with external ID and receive JWT token.
            
            **Steps to use JWT token:**
            1. Call this endpoint with your external ID
            2. Copy the `accessToken` from the response  
            3. Click 🔒 "Bearer Authentication" in Swagger UI
            4. Paste the token (without "Bearer " prefix)
            5. Click "Authorize" - now you can access protected endpoints
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Authentication failed - user not found"),
            ApiResponse(responseCode = "400", description = "Invalid request data")
        ]
    )
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val command = LoginCommand(externalId = request.externalId)
        val result = loginCommandHandler.handle(command)

        val response = LoginResponse(
            accessToken = result.accessToken,
            expiresIn = result.expiresIn,
            user = LoginResponse.UserInfo(
                id = result.user.id,
                externalId = result.user.externalId,
                name = result.user.name,
                gender = result.user.gender,
                ageRange = result.user.ageRange
            )
        )

        return ResponseEntity.ok(response)
    }
}
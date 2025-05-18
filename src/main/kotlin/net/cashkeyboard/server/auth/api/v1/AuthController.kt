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
@Tag(name = "Auth API", description = "인증 관련 API")
class AuthControllerV1(
    private val loginCommandHandler: LoginCommandHandler
) {

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "External ID를 사용하여 로그인하고 JWT 토큰을 발급받습니다",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 - 사용자를 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터"
            )
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
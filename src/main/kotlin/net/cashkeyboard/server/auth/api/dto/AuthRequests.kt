package net.cashkeyboard.server.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "External ID cannot be empty")
    @Schema(description = "외부 인증 제공자에서 제공한 사용자 ID", example = "kakao123456")
    val externalId: String
)

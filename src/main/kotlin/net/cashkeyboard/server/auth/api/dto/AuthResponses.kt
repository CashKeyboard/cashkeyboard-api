package net.cashkeyboard.server.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @Schema(description = "Token Type", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "Token expiration time in milliseconds", example = "86400000")
    val expiresIn: Long,

    @Schema(description = "User information")
    val user: UserInfo
) {
    data class UserInfo(
        val id: java.util.UUID,
        val externalId: String,
        val name: String,
        val gender: String?,
        val ageRange: String?
    )
}
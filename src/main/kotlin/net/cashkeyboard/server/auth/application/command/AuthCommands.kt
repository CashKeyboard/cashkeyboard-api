package net.cashkeyboard.server.auth.application.command

import java.util.*

data class LoginCommand(
    val externalId: String
)

data class LoginResult(
    val accessToken: String,
    val expiresIn: Long,
    val user: UserInfo
) {
    data class UserInfo(
        val id: UUID,
        val externalId: String,
        val name: String,
        val gender: String?,
        val ageRange: String?
    )
}
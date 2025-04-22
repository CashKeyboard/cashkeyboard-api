package net.cashkeyboard.server.user.api.dto

import net.cashkeyboard.server.user.domain.User
import net.cashkeyboard.server.user.domain.UserDeviceToken
import java.time.LocalDateTime
import java.util.*

data class UserResponse(
    val id: UUID,
    val externalId: String,
    val name: String,
    val gender: String?,
    val ageRange: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                externalId = user.externalId,
                name = user.name,
                gender = user.gender?.name,
                ageRange = user.ageRange?.name,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

data class UserDeviceTokenResponse(
    val id: UUID,
    val deviceToken: String,
    val deviceType: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(deviceToken: UserDeviceToken): UserDeviceTokenResponse {
            return UserDeviceTokenResponse(
                id = deviceToken.id,
                deviceToken = deviceToken.deviceToken,
                deviceType = deviceToken.deviceType,
                createdAt = deviceToken.createdAt
            )
        }
    }
}

data class UserDeviceTokensResponse(
    val userId: UUID,
    val deviceTokens: List<UserDeviceTokenResponse>
)
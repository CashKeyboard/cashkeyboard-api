package net.cashkeyboard.server.user.application.command

import net.cashkeyboard.server.user.domain.AgeRange
import net.cashkeyboard.server.user.domain.Gender
import java.util.*

data class CreateUserCommand(
    val externalId: String,
    val name: String,
    val gender: Gender? = null,
    val ageRange: AgeRange? = null
)

data class UpdateUserProfileCommand(
    val userId: UUID,
    val name: String,
    val gender: Gender? = null,
    val ageRange: AgeRange? = null
)

data class UpdateDeviceTokenCommand(
    val userId: UUID,
    val deviceToken: String,
    val deviceType: String
)

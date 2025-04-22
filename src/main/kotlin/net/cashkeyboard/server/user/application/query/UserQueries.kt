package net.cashkeyboard.server.user.application.query

import java.util.*

data class GetUserByIdQuery(
    val userId: UUID
)

data class GetUserByExternalIdQuery(
    val externalId: String
)

data class GetUserDeviceTokensQuery(
    val userId: UUID
)
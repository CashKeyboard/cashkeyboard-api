package net.cashkeyboard.server.auth.model

import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    val externalId: String,
    val name: String
)
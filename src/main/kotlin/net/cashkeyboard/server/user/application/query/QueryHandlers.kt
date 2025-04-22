package net.cashkeyboard.server.user.application.query

interface QueryHandler<T, R> {
    fun handle(query: T): R
}

interface GetUserByIdQueryHandler : QueryHandler<GetUserByIdQuery, UserDto?>
//interface GetUserByExternalIdQueryHandler : QueryHandler<GetUserByExternalIdQuery, UserDto?>
//interface GetUserDeviceTokensQueryHandler : QueryHandler<GetUserDeviceTokensQuery, List<DeviceTokenDto>>

data class UserDto(
    val id: java.util.UUID,
    val externalId: String,
    val name: String,
    val gender: String?,
    val ageRange: String?,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime
)

data class DeviceTokenDto(
    val id: java.util.UUID,
    val deviceToken: String,
    val deviceType: String,
    val createdAt: java.time.LocalDateTime
)
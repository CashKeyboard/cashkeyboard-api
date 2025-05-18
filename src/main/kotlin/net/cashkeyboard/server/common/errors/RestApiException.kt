package net.cashkeyboard.server.common.errors

open class RestApiException(
    val errorCode: ErrorCode
) : RuntimeException()

package net.cashkeyboard.server.common.errors

class RestApiException(
    val errorCode: ErrorCode
) : RuntimeException()

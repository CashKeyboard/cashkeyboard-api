package net.cashkeyboard.server.common.errors

import org.springframework.http.HttpStatus

interface ErrorCode {
    fun getCode(): String
    val httpStatus: HttpStatus
    val message: String
}
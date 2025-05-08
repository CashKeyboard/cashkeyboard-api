package net.cashkeyboard.server.common.errors

import org.springframework.http.HttpStatus

enum class CommonErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCode {
    // 공통 에러
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter included"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    // 인증 관련 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),

    // JWT 관련 에러
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid JWT token"),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "Expired JWT token"),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "Unsupported JWT token"),
    JWT_CLAIMS_EMPTY(HttpStatus.UNAUTHORIZED, "JWT claims is empty");

    override fun getCode(): String {
        return name
    }
}
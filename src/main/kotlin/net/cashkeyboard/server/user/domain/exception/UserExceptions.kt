package net.cashkeyboard.server.user.domain.exception

import net.cashkeyboard.server.common.errors.ErrorCode
import net.cashkeyboard.server.common.errors.RestApiException
import org.springframework.http.HttpStatus

enum class UserErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User with this external ID already exists"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "Invalid user data provided");

    override fun getCode(): String {
        return name
    }
}

class UserAlreadyExistsException(externalId: String) :
    RestApiException(UserErrorCode.USER_ALREADY_EXISTS)

class UserNotFoundException(userId: Any) :
    RestApiException(UserErrorCode.USER_NOT_FOUND)

class InvalidUserDataException(message: String) :
    RestApiException(UserErrorCode.INVALID_USER_DATA)
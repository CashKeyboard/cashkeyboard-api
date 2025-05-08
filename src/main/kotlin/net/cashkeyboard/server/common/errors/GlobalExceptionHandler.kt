package net.cashkeyboard.server.common.errors

import com.fasterxml.jackson.annotation.JsonInclude
import net.cashkeyboard.server.auth.exception.TokenValidationException
import net.cashkeyboard.server.auth.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // Custom exception handling
    @ExceptionHandler(RestApiException::class)
    fun handleRestApiException(e: RestApiException): ResponseEntity<ErrorResponse> {
        log.error("RestApiException: {}", e.errorCode.message)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ErrorResponse.of(e.errorCode))
    }

    // JWT token validation exception handling
    @ExceptionHandler(TokenValidationException::class)
    fun handleTokenValidationException(e: TokenValidationException): ResponseEntity<ErrorResponse> {
        log.error("TokenValidationException: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(CommonErrorCode.INVALID_JWT_TOKEN.getCode(), e.message ?: "Token validation failed"))
    }

    // Unauthorized exception handling
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        log.error("UnauthorizedException: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(CommonErrorCode.UNAUTHORIZED.getCode(), e.message ?: "Unauthorized"))
    }

    // Access denied exception handling
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.error("AccessDeniedException: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of(CommonErrorCode.ACCESS_DENIED))
    }

    // Method argument validation exception handling
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("MethodArgumentNotValidException: {}", ex.message)

        val errors = ex.bindingResult.fieldErrors.map { ErrorResponse.ValidationError.of(it) }

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.INVALID_PARAMETER.getCode(),
            "Invalid request parameters",
            errors
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    // Binding exception handling
    override fun handleBindException(
        ex: BindException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("BindException: {}", ex.message)

        val errors = ex.bindingResult.fieldErrors.map { ErrorResponse.ValidationError.of(it) }

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.INVALID_PARAMETER.getCode(),
            "Failed to bind request parameters",
            errors
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    // HTTP method not supported exception handling
    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("HttpRequestMethodNotSupportedException: {}", ex.message)

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.METHOD_NOT_ALLOWED.getCode(),
            ex.message ?: "HTTP method not supported"
        )

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(errorResponse)
    }

    // Media type not supported exception handling
    override fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("HttpMediaTypeNotSupportedException: {}", ex.message)

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
            ex.message ?: "Media type not supported"
        )

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(errorResponse)
    }

    // No handler found exception handling
    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("NoHandlerFoundException: {}", ex.message)

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.RESOURCE_NOT_FOUND.getCode(),
            ex.message ?: "Resource not found"
        )

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse)
    }

    // Method argument type mismatch exception handling
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentTypeMismatchException: {}", e.message)

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.INVALID_PARAMETER.getCode(),
            "Invalid parameter type (${e.name}: ${e.value})"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    // Generic exception handling
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception occurred", e)

        val errorResponse = ErrorResponse.of(
            CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "Internal server error"
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val errors: List<ValidationError> = emptyList()
) {

    data class ValidationError(
        val field: String,
        val message: String
    ) {
        companion object {
            fun of(fieldError: FieldError): ValidationError {
                return ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value"
                )
            }
        }
    }

    companion object {
        fun of(code: String, message: String): ErrorResponse {
            return ErrorResponse(code, message)
        }

        fun of(code: String, message: String, errors: List<ValidationError>): ErrorResponse {
            return ErrorResponse(code, message, errors)
        }

        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(errorCode.getCode(), errorCode.message)
        }

        fun of(errorCode: ErrorCode, errors: List<ValidationError>): ErrorResponse {
            return ErrorResponse(errorCode.getCode(), errorCode.message, errors)
        }
    }
}

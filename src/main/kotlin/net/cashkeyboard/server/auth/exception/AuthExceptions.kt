package net.cashkeyboard.server.auth.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Base exception class for authentication-related exceptions
 */
abstract class AuthException(message: String) : RuntimeException(message)

/**
 * Exception thrown when token validation fails
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class TokenValidationException(message: String) : AuthException(message)

/**
 * Exception thrown when authentication is required but not provided
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String) : AuthException(message)

/**
 * Exception thrown when OAuth2 authentication process fails
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class OAuth2AuthenticationProcessingException(message: String) : AuthException(message)

/**
 * Exception thrown when a requested resource is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(resourceName: String, fieldName: String, fieldValue: Any) :
    AuthException("$resourceName not found with $fieldName : '$fieldValue'")

/**
 * Exception thrown when a bad request is made
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String) : AuthException(message)
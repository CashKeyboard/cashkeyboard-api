package net.cashkeyboard.server.user.api.v1

import net.cashkeyboard.server.user.api.dto.*
import net.cashkeyboard.server.user.application.command.*
import net.cashkeyboard.server.user.application.query.*
import net.cashkeyboard.server.user.domain.AgeRange
import net.cashkeyboard.server.user.domain.Gender
import net.cashkeyboard.server.common.security.AuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "User management API")
class UserControllerV1(
    private val createUserCommandHandler: CreateUserCommandHandler,
    private val updateUserProfileCommandHandler: UpdateUserProfileCommandHandler,
    private val updateDeviceTokenCommandHandler: UpdateDeviceTokenCommandHandlerImpl,
    private val getUserByIdQueryHandler: GetUserByIdQueryHandler,
) {
    private val logger = LoggerFactory.getLogger(UserControllerV1::class.java)

    @PostMapping
    @Operation(
        summary = "User registration",
        description = "Register a new user in the system",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = [Content(schema = Schema(implementation = Map::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid user data"),
            ApiResponse(responseCode = "409", description = "User already exists")
        ]
    )
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<Map<String, UUID>> {
        val command = CreateUserCommand(
            externalId = request.externalId,
            name = request.name,
            gender = request.gender?.let { Gender.valueOf(it) },
            ageRange = request.ageRange?.let { AgeRange.valueOf(it) }
        )

        val userId = createUserCommandHandler.handle(command)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("userId" to userId))
    }

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Get user information",
        description = "Retrieve user information. Users can only access their own information.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "User retrieved successfully",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "403", description = "Access denied - can only access own information"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<UserResponse> {
        logger.debug("=== getUserById called ===")
        logger.debug("Requested user ID: $userId")

        if (!AuthUtil.isCurrentUser(userId)) {
            logger.warn("Access denied: User trying to access another user's information")
            throw AccessDeniedException("You can only access your own user information")
        }

        logger.debug("Authorization passed, querying user data")

        val query = GetUserByIdQuery(userId)
        val userDto = getUserByIdQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = UserResponse(
            id = userDto.id,
            externalId = userDto.externalId,
            name = userDto.name,
            gender = userDto.gender,
            ageRange = userDto.ageRange,
            createdAt = userDto.createdAt,
            updatedAt = userDto.updatedAt
        )

        logger.debug("Returning user response successfully")
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{userId}/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update user profile",
        description = "Update user profile information. Users can only update their own profile.",
        responses = [
            ApiResponse(responseCode = "204", description = "Profile updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid profile data"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "403", description = "Access denied - can only update own profile"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    fun updateUserProfile(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<Void> {
        if (!AuthUtil.isCurrentUser(userId)) {
            throw AccessDeniedException("You can only update your own profile")
        }

        val command = UpdateUserProfileCommand(
            userId = userId,
            name = request.name,
            gender = request.gender?.let { Gender.valueOf(it) },
            ageRange = request.ageRange?.let { AgeRange.valueOf(it) }
        )

        updateUserProfileCommandHandler.handle(command)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{userId}/device-tokens")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update device token",
        description = "Update user's device token for push notifications. Users can only update their own device token.",
        responses = [
            ApiResponse(responseCode = "204", description = "Device token updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid device token data"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "403", description = "Access denied - can only update own device token"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    fun updateDeviceToken(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserDeviceTokenRequest
    ): ResponseEntity<Void> {
        if (!AuthUtil.isCurrentUser(userId)) {
            throw AccessDeniedException("You can only update your own device token")
        }

        val command = UpdateDeviceTokenCommand(
            userId = userId,
            deviceToken = request.deviceToken,
            deviceType = request.deviceType,
        )

        updateDeviceTokenCommandHandler.handle(command)
        return ResponseEntity.noContent().build()
    }
}
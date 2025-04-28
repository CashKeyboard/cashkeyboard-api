package net.cashkeyboard.server.user.api.v1

import net.cashkeyboard.server.user.api.dto.*
import net.cashkeyboard.server.user.application.command.*
import net.cashkeyboard.server.user.application.query.*
import net.cashkeyboard.server.user.domain.AgeRange
import net.cashkeyboard.server.user.domain.Gender
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "User API")
class UserControllerV1(
    private val createUserCommandHandler: CreateUserCommandHandler,
    private val updateUserProfileCommandHandler: UpdateUserProfileCommandHandler,
    private val updateDeviceTokenCommandHandler: UpdateDeviceTokenCommandHandlerImpl,
    private val getUserByIdQueryHandler: GetUserByIdQueryHandler,
//    private val getUserByExternalIdQueryHandler: GetUserByExternalIdQueryHandler,
//    private val getUserDeviceTokensQueryHandler: GetUserDeviceTokensQueryHandler
) {

    @PostMapping
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 시스템에 등록합니다",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
                content = [Content(schema = Schema(implementation = Map::class))]
            )
        ]
    )
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<Map<String, UUID>> {
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
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<UserResponse> {
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

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{userId}/profile")
    fun updateUserProfile(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<Void> {
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
    fun updateDeviceToken(
        @PathVariable userId: UUID,
        @RequestBody request: UpdateUserDeviceTokenRequest
    ): ResponseEntity<Void> {
        val command = UpdateDeviceTokenCommand(
            userId = userId,
            deviceToken = request.deviceToken,
            deviceType = request.deviceType,
        )

        updateDeviceTokenCommandHandler.handle(command)
        return ResponseEntity.noContent().build()
    }
}
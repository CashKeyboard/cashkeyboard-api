package net.cashkeyboard.server.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import net.cashkeyboard.server.common.validation.ValidEnum
import net.cashkeyboard.server.user.domain.AgeRange
import net.cashkeyboard.server.user.domain.Gender


data class CreateUserRequest(
    @field:NotBlank(message = "External ID cannot be empty")
    @Schema(description = "외부 인증 제공자에서 제공한 사용자 ID", example = "kakao123456")
    val externalId: String,

    @field:NotBlank(message = "Name cannot be empty")
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:ValidEnum(enumClass = Gender::class)
    @Schema(
        description = "성별",
        example = "MALE",
        allowableValues = ["MALE", "FEMALE", "OTHER"]
    )
    val gender: String? = null,

    @field:ValidEnum(enumClass = AgeRange::class)
    @Schema(
        description = "연령대",
        example = "AGE_30_39",
        allowableValues = ["UNDER_10", "AGE_10_19", "AGE_20_29", "AGE_30_39", "AGE_40_49", "AGE_50_59", "AGE_60_69", "AGE_70_PLUS"]
    )
    val ageRange: String? = null
)

data class UpdateUserProfileRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,

    @field:ValidEnum(enumClass = Gender::class)
    @Schema(
        description = "성별",
        example = "MALE",
        allowableValues = ["MALE", "FEMALE", "OTHER"]
    )
    val gender: String? = null,

    @field:ValidEnum(enumClass = AgeRange::class)
    @Schema(
        description = "연령대",
        example = "AGE_30_39",
        allowableValues = ["UNDER_10", "AGE_10_19", "AGE_20_29", "AGE_30_39", "AGE_40_49", "AGE_50_59", "AGE_60_69", "AGE_70_PLUS"]
    )
    val ageRange: String? = null
)

data class UpdateUserDeviceTokenRequest(
    @field:NotBlank()
    @Schema(description = "Device Token", example = "...")
    val deviceToken: String,

    @field:NotBlank()
    @Schema(description = "Device Type", example = "...")
    val deviceType: String,
)

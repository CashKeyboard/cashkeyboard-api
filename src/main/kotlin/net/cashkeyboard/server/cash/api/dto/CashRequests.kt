package net.cashkeyboard.server.cash.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import net.cashkeyboard.server.cash.domain.EarnSource
import net.cashkeyboard.server.cash.domain.SpendPurpose
import net.cashkeyboard.server.common.validation.ValidEnum

data class EarnCashRequest(
    @field:NotNull(message = "Amount is required")
    @field:Min(value = 1, message = "Amount must be greater than 0")
    @Schema(description = "적립할 캐시 금액", example = "50", minimum = "1", maximum = "100")
    val amount: Int,

    @field:ValidEnum(enumClass = EarnSource::class)
    @Schema(
        description = "적립 소스",
        example = "TYPING",
        allowableValues = ["TYPING", "AD_WATCH", "MISSION_COMPLETE", "DAILY_BONUS", "REFERRAL"]
    )
    val source: String,

    @field:NotBlank(message = "Source ID is required")
    @Schema(description = "소스별 고유 ID (중복 방지용)", example = "ad_12345")
    val sourceId: String,

    @Schema(description = "추가 메타데이터")
    val metadata: Map<String, Any>? = null
)

data class RandomEarnCashRequest(
    @field:ValidEnum(enumClass = EarnSource::class)
    @Schema(
        description = "랜덤 적립 소스",
        example = "LUCKY_SPIN",
        allowableValues = ["LUCKY_SPIN", "RANDOM_REWARD", "SURPRISE_BONUS"]
    )
    val source: String,

    @field:NotBlank(message = "Source ID is required")
    @Schema(description = "소스별 고유 ID (중복 방지용)", example = "spin_67890")
    val sourceId: String,

    @Schema(description = "추가 메타데이터")
    val metadata: Map<String, Any>? = null
)

data class SpendCashRequest(
    @field:NotNull(message = "Amount is required")
    @field:Min(value = 1, message = "Amount must be greater than 0")
    @Schema(description = "사용할 캐시 금액", example = "500")
    val amount: Int,

    @field:ValidEnum(enumClass = SpendPurpose::class)
    @Schema(
        description = "사용 목적",
        example = "PRODUCT_PURCHASE",
        allowableValues = ["PRODUCT_PURCHASE", "PREMIUM_FEATURE", "GIFT"]
    )
    val purpose: String,

    @field:NotBlank(message = "Target ID is required")
    @Schema(description = "대상 ID (상품 ID, 기능 ID 등)", example = "product_123e4567-e89b-12d3-a456-426614174000")
    val targetId: String,

    @Schema(description = "추가 메타데이터")
    val metadata: Map<String, Any>? = null
)

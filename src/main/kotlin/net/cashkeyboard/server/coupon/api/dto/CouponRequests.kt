package net.cashkeyboard.server.coupon.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

data class PurchaseCouponRequest(
    @field:NotNull(message = "Product ID is required")
    @Schema(description = "상품 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val productId: UUID,

    @Schema(description = "구매 메타데이터")
    val metadata: Map<String, Any>? = null
)

data class AdminIssueCouponRequest(
    @field:NotNull(message = "Target user ID is required")
    @Schema(description = "대상 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val targetUserId: UUID,

    @field:NotNull(message = "Product ID is required")
    @Schema(description = "상품 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val productId: UUID,

    @field:NotBlank(message = "Issue reason is required")
    @Schema(description = "발급 사유", example = "프로모션 이벤트 참여 보상")
    val issueReason: String,

    @field:Future(message = "Expiration date must be in the future")
    @Schema(description = "만료일시 (미래 시간)", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,

    @Schema(description = "발급 메타데이터")
    val metadata: Map<String, Any>? = null
)

data class CancelCouponRequest(
    @field:NotBlank(message = "Cancellation reason is required")
    @Schema(description = "취소 사유", example = "사용자 요청에 의한 취소")
    val reason: String,

    @field:Min(value = 0, message = "Refund amount must be non-negative")
    @Schema(description = "환불 금액", example = "4500")
    val refundAmount: Int = 0,

    @Schema(description = "취소 메타데이터")
    val metadata: Map<String, Any>? = null
)

data class UseCouponRequest(
    @Schema(description = "사용 메타데이터 (매장 정보 등)")
    val metadata: Map<String, Any>? = null
)
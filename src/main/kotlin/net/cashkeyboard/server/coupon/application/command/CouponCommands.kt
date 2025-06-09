package net.cashkeyboard.server.coupon.application.command

import java.time.LocalDateTime
import java.util.*

// Commands
data class PurchaseCouponCommand(
    val userId: UUID,
    val productId: UUID,
    val metadata: Map<String, Any>? = null
)

data class AdminIssueCouponCommand(
    val adminId: String,
    val targetUserId: UUID,
    val productId: UUID,
    val issueReason: String,
    val expiresAt: LocalDateTime,
    val metadata: Map<String, Any>? = null
)

data class CancelCouponCommand(
    val couponId: UUID,
    val adminId: String,
    val reason: String,
    val refundAmount: Int = 0,
    val metadata: Map<String, Any>? = null
)

data class UseCouponCommand(
    val couponId: UUID,
    val userId: UUID,
    val metadata: Map<String, Any>? = null
)

data class UpdateGifticonInfoCommand(
    val couponId: UUID,
    val couponCode: String,
    val couponImageUrl: String
)

// Command Results
data class PurchaseCouponResult(
    val couponId: UUID,
    val transactionId: UUID,
    val deductedAmount: Int,
    val newCashBalance: Int,
    val expiresAt: LocalDateTime,
    val purchasedAt: LocalDateTime
)

data class AdminIssueCouponResult(
    val couponId: UUID,
    val targetUserId: UUID,
    val expiresAt: LocalDateTime,
    val issuedAt: LocalDateTime
)

data class CancelCouponResult(
    val couponId: UUID,
    val refundAmount: Int,
    val refundTransactionId: UUID?,
    val cancelledAt: LocalDateTime
)

data class UseCouponResult(
    val couponId: UUID,
    val usedAt: LocalDateTime
)

data class UpdateGifticonInfoResult(
    val couponId: UUID,
    val couponCode: String,
    val couponImageUrl: String,
    val updatedAt: LocalDateTime
)
package net.cashkeyboard.server.coupon.domain.service

import net.cashkeyboard.server.coupon.domain.*
import net.cashkeyboard.server.coupon.domain.exception.*
import net.cashkeyboard.server.product.domain.Product
import net.cashkeyboard.server.user.domain.User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class CouponDomainService {

    /**
     * Validate if user can purchase coupon for the product
     */
    fun validateCouponPurchase(user: User, product: Product, userCashBalance: Int) {
        // Check if product is purchasable
        if (!product.isPurchasable()) {
            throw ProductNotAvailableForCouponException(product.id)
        }

        // Check if user has sufficient cash
        if (userCashBalance < product.price) {
            throw InsufficientCashForCouponException(product.price, userCashBalance)
        }
    }

    /**
     * Calculate coupon expiration date based on product type
     */
    fun calculateExpirationDate(product: Product): LocalDateTime {
        return when (product.category) {
            // Coffee and food items expire in 30 days
            net.cashkeyboard.server.product.domain.ProductCategory.COFFEE,
            net.cashkeyboard.server.product.domain.ProductCategory.FOOD ->
                LocalDateTime.now().plusDays(30)

            // Gift cards expire in 1 year
            net.cashkeyboard.server.product.domain.ProductCategory.GIFT_CARD ->
                LocalDateTime.now().plusYears(1)

            // Digital items expire in 90 days
            net.cashkeyboard.server.product.domain.ProductCategory.DIGITAL,
            net.cashkeyboard.server.product.domain.ProductCategory.ENTERTAINMENT ->
                LocalDateTime.now().plusDays(90)

            // Other items expire in 60 days (default)
            else -> LocalDateTime.now().plusDays(60)
        }
    }

    /**
     * Generate notification content for coupon issuance
     */
    fun generateCouponIssuanceNotification(coupon: Coupon, product: Product): NotificationContent {
        val title = when (coupon.issueType) {
            CouponIssueType.PURCHASE -> "쿠폰 구매 완료"
            CouponIssueType.ADMIN_ISSUE -> "쿠폰이 발급되었습니다"
            CouponIssueType.PROMOTION -> "프로모션 쿠폰 지급"
            CouponIssueType.COMPENSATION -> "보상 쿠폰 지급"
        }

        val body = when (coupon.issueType) {
            CouponIssueType.PURCHASE -> "${product.name} 쿠폰이 성공적으로 구매되었습니다."
            CouponIssueType.ADMIN_ISSUE -> "${product.name} 쿠폰이 발급되었습니다. ${coupon.issueReason ?: ""}"
            CouponIssueType.PROMOTION -> "${product.name} 프로모션 쿠폰을 받으셨습니다!"
            CouponIssueType.COMPENSATION -> "${product.name} 보상 쿠폰이 지급되었습니다."
        }

        val dataPayload = mapOf(
            "type" to "coupon_issued",
            "couponId" to coupon.id.toString(),
            "productId" to product.id.toString(),
            "productName" to product.name,
            "issueType" to coupon.issueType.name,
            "expiresAt" to coupon.expiresAt.toString()
        )

        return NotificationContent(title, body, dataPayload)
    }

    /**
     * Generate notification content for coupon expiration warning
     */
    fun generateExpirationWarningNotification(coupon: Coupon, product: Product): NotificationContent {
        val title = "쿠폰 만료 예정"
        val body = "${product.name} 쿠폰이 곧 만료됩니다. 만료일: ${coupon.expiresAt.toLocalDate()}"

        val dataPayload = mapOf(
            "type" to "coupon_expiring",
            "couponId" to coupon.id.toString(),
            "productId" to product.id.toString(),
            "productName" to product.name,
            "expiresAt" to coupon.expiresAt.toString()
        )

        return NotificationContent(title, body, dataPayload)
    }

    /**
     * Validate admin coupon issuance
     */
    fun validateAdminCouponIssuance(
        targetUser: User,
        product: Product,
        issueReason: String,
        expirationDate: LocalDateTime
    ) {
        // Validate issue reason
        if (issueReason.isBlank()) {
            throw InvalidCouponDataException("Issue reason is required for admin issued coupons")
        }

        // Validate expiration date
        if (expirationDate.isBefore(LocalDateTime.now())) {
            throw InvalidExpirationDateException(expirationDate)
        }

        // Product should be active (but stock check is not required for admin issue)
        if (!product.isActive()) {
            throw ProductNotAvailableForCouponException(product.id)
        }
    }

    /**
     * Validate coupon cancellation
     */
    fun validateCouponCancellation(coupon: Coupon, adminId: String, refundAmount: Int) {
        // Check if coupon can be cancelled
        if (!coupon.isCancellable()) {
            throw CouponNotCancellableException(coupon.id, coupon.status)
        }

        // Validate refund amount
        if (refundAmount < 0) {
            throw InvalidRefundAmountException(refundAmount, coupon.paidAmount)
        }

        if (refundAmount > coupon.paidAmount) {
            throw InvalidRefundAmountException(refundAmount, coupon.paidAmount)
        }
    }

    /**
     * Calculate refund amount for cancelled coupon
     */
    fun calculateRefundAmount(coupon: Coupon): Int {
        return when (coupon.issueType) {
            CouponIssueType.PURCHASE -> coupon.paidAmount // Full refund for purchased coupons
            CouponIssueType.ADMIN_ISSUE,
            CouponIssueType.PROMOTION,
            CouponIssueType.COMPENSATION -> 0 // No refund for free coupons
        }
    }

    /**
     * Check if user has permission to access coupon
     */
    fun validateCouponAccess(coupon: Coupon, userId: UUID) {
        if (coupon.userId != userId) {
            throw CouponAccessDeniedException(coupon.id, userId)
        }
    }

    data class NotificationContent(
        val title: String,
        val body: String,
        val dataPayload: Map<String, String>
    )
}
package net.cashkeyboard.server.coupon.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import net.cashkeyboard.server.coupon.domain.exception.CouponAlreadyUsedException
import net.cashkeyboard.server.coupon.domain.exception.CouponExpiredException
import net.cashkeyboard.server.coupon.domain.exception.CouponNotActiveException
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "coupons")
class Coupon(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    /**
     * User who owns this coupon
     */
    @Column(nullable = false)
    val userId: UUID,

    /**
     * Product that this coupon represents
     */
    @Column(nullable = false)
    val productId: UUID,

    /**
     * Original product price
     */
    @Column(nullable = false)
    val originalPrice: Int,

    /**
     * Amount actually paid by user (0 for admin issued coupons)
     */
    @Column(nullable = false)
    val paidAmount: Int = 0,

    /**
     * How this coupon was issued
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val issueType: CouponIssueType,

    /**
     * Reason for issuing this coupon (for admin issued coupons)
     */
    @Column(nullable = true)
    val issueReason: String? = null,

    /**
     * Coupon code from external gifticon provider
     */
    @Column(nullable = true)
    var couponCode: String? = null,

    /**
     * Coupon image URL from external gifticon provider
     */
    @Column(nullable = true)
    var couponImageUrl: String? = null,

    /**
     * Current coupon status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CouponStatus = CouponStatus.ACTIVE,

    /**
     * Coupon expiration date
     */
    @Column(nullable = false)
    var expiresAt: LocalDateTime,

    /**
     * When coupon was used
     */
    @Column(nullable = true)
    var usedAt: LocalDateTime? = null,

    /**
     * When coupon was cancelled
     */
    @Column(nullable = true)
    var cancelledAt: LocalDateTime? = null,

    /**
     * Refund amount (if cancelled)
     */
    @Column(nullable = false)
    var refundAmount: Int = 0,

    /**
     * Admin user ID who cancelled this coupon
     */
    @Column(nullable = true)
    var cancelledByAdminId: String? = null,

    /**
     * Additional metadata in JSON format
     */
    @Column(columnDefinition = "TEXT")
    var metadata: String? = null

) : BaseTimeEntity() {

    /**
     * Check if coupon is currently usable
     */
    fun isUsable(): Boolean {
        return status == CouponStatus.ACTIVE && !isExpired()
    }

    /**
     * Check if coupon is expired
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    /**
     * Check if coupon can be cancelled
     */
    fun isCancellable(): Boolean {
        return status == CouponStatus.ACTIVE
    }

    /**
     * Use this coupon
     */
    fun use() {
        if (status != CouponStatus.ACTIVE) {
            throw CouponNotActiveException(id, status)
        }

        if (isExpired()) {
            throw CouponExpiredException(id, expiresAt)
        }

        if (usedAt != null) {
            throw CouponAlreadyUsedException(id, usedAt!!)
        }

        status = CouponStatus.USED
        usedAt = LocalDateTime.now()
    }

    /**
     * Cancel this coupon (admin operation)
     */
    fun cancel(adminId: String, refundAmount: Int = 0) {
        if (!isCancellable()) {
            throw CouponNotActiveException(id, status)
        }

        status = CouponStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
        cancelledByAdminId = adminId
        this.refundAmount = refundAmount
    }

    /**
     * Process refund for this coupon
     */
    fun processRefund(refundAmount: Int) {
        require(status == CouponStatus.CANCELLED) { "Coupon must be cancelled before refund" }
        require(refundAmount >= 0) { "Refund amount must be non-negative" }
        require(refundAmount <= paidAmount) { "Refund amount cannot exceed paid amount" }

        status = CouponStatus.REFUNDED
        this.refundAmount = refundAmount
    }

    /**
     * Mark coupon as expired
     */
    fun markAsExpired() {
        if (status == CouponStatus.ACTIVE && isExpired()) {
            status = CouponStatus.EXPIRED
        }
    }

    /**
     * Update gifticon information after external API call
     */
    fun updateGifticonInfo(couponCode: String, couponImageUrl: String) {
        require(status == CouponStatus.ACTIVE) { "Can only update gifticon info for active coupons" }

        this.couponCode = couponCode
        this.couponImageUrl = couponImageUrl
    }

    /**
     * Extend expiration date (admin operation)
     */
    fun extendExpiration(newExpiresAt: LocalDateTime, adminId: String) {
        require(newExpiresAt.isAfter(LocalDateTime.now())) { "New expiration date must be in the future" }
        require(status == CouponStatus.ACTIVE) { "Can only extend expiration for active coupons" }

        this.expiresAt = newExpiresAt
    }

    companion object {
        /**
         * Create coupon from purchase
         */
        fun fromPurchase(
            userId: UUID,
            productId: UUID,
            originalPrice: Int,
            paidAmount: Int,
            expiresAt: LocalDateTime
        ): Coupon {
            return Coupon(
                userId = userId,
                productId = productId,
                originalPrice = originalPrice,
                paidAmount = paidAmount,
                issueType = CouponIssueType.PURCHASE,
                expiresAt = expiresAt
            )
        }

        /**
         * Create coupon from admin issue
         */
        fun fromAdminIssue(
            userId: UUID,
            productId: UUID,
            originalPrice: Int,
            issueReason: String,
            expiresAt: LocalDateTime
        ): Coupon {
            return Coupon(
                userId = userId,
                productId = productId,
                originalPrice = originalPrice,
                paidAmount = 0,
                issueType = CouponIssueType.ADMIN_ISSUE,
                issueReason = issueReason,
                expiresAt = expiresAt
            )
        }
    }
}
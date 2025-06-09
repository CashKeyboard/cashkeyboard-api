package net.cashkeyboard.server.coupon.application.query

import net.cashkeyboard.server.coupon.domain.CouponIssueType
import net.cashkeyboard.server.coupon.domain.CouponStatus
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

// Queries
data class GetCouponByIdQuery(
    val couponId: UUID,
    val userId: UUID? = null // For access control
)

data class GetUserCouponsQuery(
    val userId: UUID,
    val pageable: Pageable,
    val status: CouponStatus? = null,
    val productId: UUID? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class GetCouponsForAdminQuery(
    val pageable: Pageable,
    val userId: UUID? = null,
    val productId: UUID? = null,
    val status: CouponStatus? = null,
    val issueType: CouponIssueType? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class GetCouponStatisticsQuery(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val groupBy: StatisticsGroupBy = StatisticsGroupBy.MONTH
) {
    enum class StatisticsGroupBy {
        DAY, WEEK, MONTH, YEAR
    }
}

data class VerifyCouponCodeQuery(
    val couponCode: String
)

// DTOs
data class CouponDto(
    val id: UUID,
    val userId: UUID,
    val productId: UUID,
    val productName: String,
    val productDescription: String,
    val productImageUrl: String,
    val productCategory: String,
    val productCategoryDisplayName: String,
    val originalPrice: Int,
    val paidAmount: Int,
    val issueType: String,
    val issueTypeDisplayName: String,
    val issueReason: String?,
    val couponCode: String?,
    val couponImageUrl: String?,
    val status: String,
    val statusDisplayName: String,
    val expiresAt: LocalDateTime,
    val usedAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
    val refundAmount: Int,
    val cancelledByAdminId: String?,
    val isUsable: Boolean,
    val isExpired: Boolean,
    val metadata: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CouponSummaryDto(
    val id: UUID,
    val productName: String,
    val productImageUrl: String,
    val originalPrice: Int,
    val paidAmount: Int,
    val status: String,
    val statusDisplayName: String,
    val expiresAt: LocalDateTime,
    val isUsable: Boolean,
    val createdAt: LocalDateTime
)

data class CouponStatisticsDto(
    val period: String,
    val totalIssued: Long,
    val purchasedCount: Long,
    val adminIssuedCount: Long,
    val promotionCount: Long,
    val totalUsed: Long,
    val totalCancelled: Long,
    val totalExpired: Long,
    val totalRevenue: Long,
    val totalRefund: Long,
    val usageRate: Double,
    val cancellationRate: Double
)

data class CouponVerificationDto(
    val couponId: UUID,
    val productName: String,
    val status: String,
    val isUsable: Boolean,
    val expiresAt: LocalDateTime,
    val ownerUserId: UUID
)
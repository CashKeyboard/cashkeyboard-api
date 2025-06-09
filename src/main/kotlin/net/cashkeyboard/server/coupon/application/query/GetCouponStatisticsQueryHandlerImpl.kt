package net.cashkeyboard.server.coupon.application.query

import net.cashkeyboard.server.coupon.domain.CouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCouponStatisticsQueryHandlerImpl(
    private val couponRepository: CouponRepository
) : GetCouponStatisticsQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetCouponStatisticsQuery): CouponStatisticsDto {
        // Basic implementation - can be enhanced with more sophisticated statistics
        val totalIssued = couponRepository.countCouponsIssuedBetween(query.startDate, query.endDate)
        val totalUsed = couponRepository.countByStatus(net.cashkeyboard.server.coupon.domain.CouponStatus.USED)
        val totalCancelled = couponRepository.countByStatus(net.cashkeyboard.server.coupon.domain.CouponStatus.CANCELLED)
        val totalExpired = couponRepository.countByStatus(net.cashkeyboard.server.coupon.domain.CouponStatus.EXPIRED)

        val purchasedCount = couponRepository.sumPaidAmountsByIssueTypeAndDateRange(
            net.cashkeyboard.server.coupon.domain.CouponIssueType.PURCHASE,
            query.startDate,
            query.endDate
        )

        val adminIssuedCount = couponRepository.sumPaidAmountsByIssueTypeAndDateRange(
            net.cashkeyboard.server.coupon.domain.CouponIssueType.ADMIN_ISSUE,
            query.startDate,
            query.endDate
        )

        val promotionCount = couponRepository.sumPaidAmountsByIssueTypeAndDateRange(
            net.cashkeyboard.server.coupon.domain.CouponIssueType.PROMOTION,
            query.startDate,
            query.endDate
        )

        val usageRate = if (totalIssued > 0) (totalUsed.toDouble() / totalIssued) * 100 else 0.0
        val cancellationRate = if (totalIssued > 0) (totalCancelled.toDouble() / totalIssued) * 100 else 0.0

        return CouponStatisticsDto(
            period = "${query.startDate.toLocalDate()} ~ ${query.endDate.toLocalDate()}",
            totalIssued = totalIssued,
            purchasedCount = purchasedCount,
            adminIssuedCount = adminIssuedCount,
            promotionCount = promotionCount,
            totalUsed = totalUsed,
            totalCancelled = totalCancelled,
            totalExpired = totalExpired,
            totalRevenue = purchasedCount, // Simplified - should be more detailed
            totalRefund = couponRepository.sumRefundAmountsByDateRange(query.startDate, query.endDate),
            usageRate = usageRate,
            cancellationRate = cancellationRate
        )
    }
}
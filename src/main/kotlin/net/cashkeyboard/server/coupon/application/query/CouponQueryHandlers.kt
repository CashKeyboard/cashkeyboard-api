package net.cashkeyboard.server.coupon.application.query

import org.springframework.data.domain.Page

interface QueryHandler<T, R> {
    fun handle(query: T): R
}

interface GetCouponByIdQueryHandler : QueryHandler<GetCouponByIdQuery, CouponDto?>

interface GetUserCouponsQueryHandler : QueryHandler<GetUserCouponsQuery, Page<CouponSummaryDto>>

interface GetCouponsForAdminQueryHandler : QueryHandler<GetCouponsForAdminQuery, Page<CouponDto>>

interface GetCouponStatisticsQueryHandler : QueryHandler<GetCouponStatisticsQuery, CouponStatisticsDto>

interface VerifyCouponCodeQueryHandler : QueryHandler<VerifyCouponCodeQuery, CouponVerificationDto?>
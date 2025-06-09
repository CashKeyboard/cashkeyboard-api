package net.cashkeyboard.server.coupon.application.query

import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserCouponsQueryHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository
) : GetUserCouponsQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetUserCouponsQuery): org.springframework.data.domain.Page<CouponSummaryDto> {
        val coupons = if (query.status != null) {
            couponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                query.userId,
                query.status,
                query.pageable
            )
        } else {
            couponRepository.findByUserIdOrderByCreatedAtDesc(query.userId, query.pageable)
        }

        return coupons.map { coupon ->
            val product = productRepository.findById(coupon.productId).orElse(null)

            CouponSummaryDto(
                id = coupon.id,
                productName = product?.name ?: "Unknown Product",
                productImageUrl = product?.imageUrl ?: "",
                originalPrice = coupon.originalPrice,
                paidAmount = coupon.paidAmount,
                status = coupon.status.name,
                statusDisplayName = coupon.status.displayName,
                expiresAt = coupon.expiresAt,
                isUsable = coupon.isUsable(),
                createdAt = coupon.createdAt
            )
        }
    }
}
package net.cashkeyboard.server.coupon.application.query

import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VerifyCouponCodeQueryHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository
) : VerifyCouponCodeQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: VerifyCouponCodeQuery): CouponVerificationDto? {
        val coupon = couponRepository.findByCouponCode(query.couponCode).orElse(null) ?: return null
        val product = productRepository.findById(coupon.productId).orElse(null) ?: return null

        return CouponVerificationDto(
            couponId = coupon.id,
            productName = product.name,
            status = coupon.status.name,
            isUsable = coupon.isUsable(),
            expiresAt = coupon.expiresAt,
            ownerUserId = coupon.userId
        )
    }
}
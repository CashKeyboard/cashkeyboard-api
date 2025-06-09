package net.cashkeyboard.server.coupon.application.command

import net.cashkeyboard.server.coupon.domain.CouponRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateGifticonInfoCommandHandlerImpl(
    private val couponRepository: CouponRepository
) : UpdateGifticonInfoCommandHandler {

    private val logger = LoggerFactory.getLogger(UpdateGifticonInfoCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: UpdateGifticonInfoCommand): UpdateGifticonInfoResult {
        logger.debug("Updating gifticon info: couponId=${command.couponId}")

        // 1. Find coupon
        val coupon = couponRepository.findById(command.couponId)
            .orElseThrow { net.cashkeyboard.server.coupon.domain.exception.CouponNotFoundException(command.couponId) }

        // 2. Update gifticon info
        coupon.updateGifticonInfo(command.couponCode, command.couponImageUrl)

        // 3. Save coupon
        val savedCoupon = couponRepository.save(coupon)

        logger.info("Gifticon info updated: couponId=${command.couponId}, couponCode=${command.couponCode}")

        return UpdateGifticonInfoResult(
            couponId = savedCoupon.id,
            couponCode = command.couponCode,
            couponImageUrl = command.couponImageUrl,
            updatedAt = savedCoupon.updatedAt
        )
    }
}
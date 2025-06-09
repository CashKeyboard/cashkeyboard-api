package net.cashkeyboard.server.coupon.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.coupon.domain.service.CouponDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UseCouponCommandHandlerImpl(
    private val couponRepository: CouponRepository,
    private val couponDomainService: CouponDomainService,
    private val objectMapper: ObjectMapper
) : UseCouponCommandHandler {

    private val logger = LoggerFactory.getLogger(UseCouponCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: UseCouponCommand): UseCouponResult {
        logger.debug("Processing coupon use: couponId=${command.couponId}, userId=${command.userId}")

        // 1. Find coupon
        val coupon = couponRepository.findById(command.couponId)
            .orElseThrow { net.cashkeyboard.server.coupon.domain.exception.CouponNotFoundException(command.couponId) }

        // 2. Validate user access
        couponDomainService.validateCouponAccess(coupon, command.userId)

        // 3. Use coupon
        coupon.use()

        // 4. Add metadata if provided
        if (command.metadata != null) {
            val existingMetadata = coupon.metadata?.let {
                objectMapper.readValue(it, Map::class.java) as Map<String, Any>
            } ?: emptyMap()

            val updatedMetadata = existingMetadata + mapOf("usageInfo" to command.metadata)
            coupon.metadata = objectMapper.writeValueAsString(updatedMetadata)
        }

        // 5. Save coupon
        val savedCoupon = couponRepository.save(coupon)

        logger.info("Coupon used successfully: couponId=${command.couponId}, userId=${command.userId}")

        return UseCouponResult(
            couponId = savedCoupon.id,
            usedAt = savedCoupon.usedAt!!
        )
    }
}
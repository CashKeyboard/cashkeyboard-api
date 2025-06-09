package net.cashkeyboard.server.coupon.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.coupon.domain.service.CouponDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelCouponCommandHandlerImpl(
    private val couponRepository: CouponRepository,
    private val couponDomainService: CouponDomainService,
    private val objectMapper: ObjectMapper
    // TODO: Add cash refund handler when needed
) : CancelCouponCommandHandler {

    private val logger = LoggerFactory.getLogger(CancelCouponCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: CancelCouponCommand): CancelCouponResult {
        logger.debug("Processing coupon cancellation: couponId=${command.couponId}, adminId=${command.adminId}")

        // 1. Find coupon
        val coupon = couponRepository.findById(command.couponId)
            .orElseThrow { net.cashkeyboard.server.coupon.domain.exception.CouponNotFoundException(command.couponId) }

        // 2. Validate cancellation
        couponDomainService.validateCouponCancellation(coupon, command.adminId, command.refundAmount)

        // 3. Cancel coupon
        coupon.cancel(command.adminId, command.refundAmount)

        // 4. Add metadata if provided
        if (command.metadata != null) {
            val existingMetadata = coupon.metadata?.let {
                objectMapper.readValue(it, Map::class.java) as Map<String, Any>
            } ?: emptyMap()

            val updatedMetadata = existingMetadata + mapOf(
                "cancellationInfo" to command.metadata,
                "cancellationReason" to command.reason
            )
            coupon.metadata = objectMapper.writeValueAsString(updatedMetadata)
        }

        // 5. Save coupon
        val savedCoupon = couponRepository.save(coupon)

        // 6. Process refund if needed
        var refundTransactionId: java.util.UUID? = null
        if (command.refundAmount > 0) {
            // TODO: Implement cash refund logic
            logger.info("Refund processing needed: amount=${command.refundAmount}")
        }

        logger.info("Coupon cancelled successfully: couponId=${command.couponId}, adminId=${command.adminId}")

        return CancelCouponResult(
            couponId = savedCoupon.id,
            refundAmount = command.refundAmount,
            refundTransactionId = refundTransactionId,
            cancelledAt = savedCoupon.cancelledAt!!
        )
    }
}
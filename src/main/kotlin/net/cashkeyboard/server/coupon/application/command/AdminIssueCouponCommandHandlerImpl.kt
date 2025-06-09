package net.cashkeyboard.server.coupon.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.coupon.domain.Coupon
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.coupon.domain.service.CouponDomainService
import net.cashkeyboard.server.product.domain.ProductRepository
import net.cashkeyboard.server.product.domain.exception.ProductNotFoundException
import net.cashkeyboard.server.user.domain.UserRepository
import net.cashkeyboard.server.user.domain.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminIssueCouponCommandHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val couponDomainService: CouponDomainService,
    private val objectMapper: ObjectMapper
) : AdminIssueCouponCommandHandler {

    private val logger = LoggerFactory.getLogger(AdminIssueCouponCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: AdminIssueCouponCommand): AdminIssueCouponResult {
        logger.debug("Processing admin coupon issue: adminId=${command.adminId}, targetUserId=${command.targetUserId}")

        // 1. Validate target user exists
        val targetUser = userRepository.findById(command.targetUserId)
            .orElseThrow { UserNotFoundException(command.targetUserId) }

        // 2. Validate product exists
        val product = productRepository.findById(command.productId)
            .orElseThrow { ProductNotFoundException(command.productId) }

        // 3. Validate admin issuance
        couponDomainService.validateAdminCouponIssuance(
            targetUser = targetUser,
            product = product,
            issueReason = command.issueReason,
            expirationDate = command.expiresAt
        )

        // 4. Create coupon
        val coupon = Coupon.fromAdminIssue(
            userId = command.targetUserId,
            productId = command.productId,
            originalPrice = product.price,
            issueReason = command.issueReason,
            expiresAt = command.expiresAt
        )

        // 5. Add metadata if provided
        if (command.metadata != null) {
            coupon.metadata = objectMapper.writeValueAsString(command.metadata)
        }

        // 6. Save coupon
        val savedCoupon = couponRepository.save(coupon)

        logger.info("Admin coupon issued successfully: couponId=${savedCoupon.id}, adminId=${command.adminId}")

        // TODO: Trigger gifticon API integration (async)
        // TODO: Send FCM notification (async)

        return AdminIssueCouponResult(
            couponId = savedCoupon.id,
            targetUserId = command.targetUserId,
            expiresAt = command.expiresAt,
            issuedAt = savedCoupon.createdAt
        )
    }
}

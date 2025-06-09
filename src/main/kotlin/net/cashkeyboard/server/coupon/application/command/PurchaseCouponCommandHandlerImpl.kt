package net.cashkeyboard.server.coupon.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.cash.application.command.SpendCashCommand
import net.cashkeyboard.server.cash.application.command.SpendCashCommandHandler
import net.cashkeyboard.server.cash.domain.SpendPurpose
import net.cashkeyboard.server.coupon.domain.Coupon
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.coupon.domain.service.CouponDomainService
import net.cashkeyboard.server.coupon.domain.exception.ProductNotAvailableForCouponException
import net.cashkeyboard.server.product.domain.ProductRepository
import net.cashkeyboard.server.product.domain.exception.ProductNotFoundException
import net.cashkeyboard.server.user.domain.UserRepository
import net.cashkeyboard.server.user.domain.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PurchaseCouponCommandHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val spendCashCommandHandler: SpendCashCommandHandler,
    private val couponDomainService: CouponDomainService,
    private val objectMapper: ObjectMapper
) : PurchaseCouponCommandHandler {

    private val logger = LoggerFactory.getLogger(PurchaseCouponCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: PurchaseCouponCommand): PurchaseCouponResult {
        logger.debug("Processing coupon purchase: userId=${command.userId}, productId=${command.productId}")

        // 1. Validate user exists
        val user = userRepository.findById(command.userId)
            .orElseThrow { UserNotFoundException(command.userId) }

        // 2. Validate product exists and is purchasable
        val product = productRepository.findById(command.productId)
            .orElseThrow { ProductNotFoundException(command.productId) }

        if (!product.isPurchasable()) {
            throw ProductNotAvailableForCouponException(command.productId)
        }

        // 3. Calculate expiration date
        val expiresAt = couponDomainService.calculateExpirationDate(product)

        // 4. Process cash spending
        val spendCashCommand = SpendCashCommand(
            userId = command.userId,
            amount = product.price,
            purpose = SpendPurpose.PRODUCT_PURCHASE,
            targetId = command.productId.toString(),
            metadata = command.metadata
        )

        val spendResult = spendCashCommandHandler.handle(spendCashCommand)

        // 5. Create coupon
        val coupon = Coupon.fromPurchase(
            userId = command.userId,
            productId = command.productId,
            originalPrice = product.price,
            paidAmount = product.price,
            expiresAt = expiresAt
        )

        // 6. Add metadata if provided
        if (command.metadata != null) {
            coupon.metadata = objectMapper.writeValueAsString(command.metadata)
        }

        // 7. Save coupon
        val savedCoupon = couponRepository.save(coupon)

        // 8. Decrease product stock
        product.decreaseStock(1)
        productRepository.save(product)

        logger.info("Coupon purchased successfully: couponId=${savedCoupon.id}, userId=${command.userId}")

        // TODO: Trigger gifticon API integration (async)
        // TODO: Send FCM notification (async)

        return PurchaseCouponResult(
            couponId = savedCoupon.id,
            transactionId = spendResult.transactionId,
            deductedAmount = spendResult.spentAmount,
            newCashBalance = spendResult.newBalance,
            expiresAt = expiresAt,
            purchasedAt = savedCoupon.createdAt
        )
    }
}
package net.cashkeyboard.server.coupon.application.query

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCouponByIdQueryHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper
) : GetCouponByIdQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetCouponByIdQuery): CouponDto? {
        val coupon = couponRepository.findById(query.couponId).orElse(null) ?: return null

        // Access control for user queries
        if (query.userId != null && coupon.userId != query.userId) {
            return null
        }

        val product = productRepository.findById(coupon.productId).orElse(null) ?: return null

        val metadata: Map<String, Any>? = coupon.metadata?.let { metadataStr ->
            try {
                objectMapper.readValue<Map<String, Any>>(metadataStr)
            } catch (e: Exception) {
                null
            }
        }

        return CouponDto(
            id = coupon.id,
            userId = coupon.userId,
            productId = product.id,
            productName = product.name,
            productDescription = product.description,
            productImageUrl = product.imageUrl,
            productCategory = product.category.name,
            productCategoryDisplayName = product.category.displayName,
            originalPrice = coupon.originalPrice,
            paidAmount = coupon.paidAmount,
            issueType = coupon.issueType.name,
            issueTypeDisplayName = coupon.issueType.displayName,
            issueReason = coupon.issueReason,
            couponCode = coupon.couponCode,
            couponImageUrl = coupon.couponImageUrl,
            status = coupon.status.name,
            statusDisplayName = coupon.status.displayName,
            expiresAt = coupon.expiresAt,
            usedAt = coupon.usedAt,
            cancelledAt = coupon.cancelledAt,
            refundAmount = coupon.refundAmount,
            cancelledByAdminId = coupon.cancelledByAdminId,
            isUsable = coupon.isUsable(),
            isExpired = coupon.isExpired(),
            metadata = metadata,
            createdAt = coupon.createdAt,
            updatedAt = coupon.updatedAt
        )
    }
}
package net.cashkeyboard.server.coupon.application.query

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.cashkeyboard.server.coupon.domain.CouponRepository
import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCouponsForAdminQueryHandlerImpl(
    private val couponRepository: CouponRepository,
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper
) : GetCouponsForAdminQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetCouponsForAdminQuery): org.springframework.data.domain.Page<CouponDto> {
        val coupons = couponRepository.findCouponsWithFilters(
            userId = query.userId,
            productId = query.productId,
            status = query.status,
            issueType = query.issueType,
            startDate = query.startDate,
            endDate = query.endDate,
            pageable = query.pageable
        )

        return coupons.map { coupon ->
            val product = productRepository.findById(coupon.productId).orElse(null)

            val metadata: Map<String, Any>? = coupon.metadata?.let { metadataStr ->
                try {
                    objectMapper.readValue<Map<String, Any>>(metadataStr)
                } catch (e: Exception) {
                    null
                }
            }

            CouponDto(
                id = coupon.id,
                userId = coupon.userId,
                productId = coupon.productId,
                productName = product?.name ?: "Unknown Product",
                productDescription = product?.description ?: "",
                productImageUrl = product?.imageUrl ?: "",
                productCategory = product?.category?.name ?: "",
                productCategoryDisplayName = product?.category?.displayName ?: "",
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
}

package net.cashkeyboard.server.coupon.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

data class CouponResponse(
    @Schema(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val userId: UUID,

    @Schema(description = "상품 정보")
    val product: ProductInfo,

    @Schema(description = "원래 상품 가격", example = "4500")
    val originalPrice: Int,

    @Schema(description = "실제 지불 금액", example = "4500")
    val paidAmount: Int,

    @Schema(description = "발급 타입", example = "PURCHASE")
    val issueType: String,

    @Schema(description = "발급 사유", example = "프로모션 이벤트")
    val issueReason: String?,

    @Schema(description = "쿠폰 코드", example = "GIFT123456789")
    val couponCode: String?,

    @Schema(description = "쿠폰 이미지 URL", example = "https://example.com/coupon.jpg")
    val couponImageUrl: String?,

    @Schema(description = "쿠폰 상태", example = "ACTIVE")
    val status: String,

    @Schema(description = "쿠폰 상태 표시명", example = "활성")
    val statusDisplayName: String,

    @Schema(description = "만료일시", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,

    @Schema(description = "사용일시", example = "2024-06-15T14:30:00")
    val usedAt: LocalDateTime?,

    @Schema(description = "취소일시", example = "2024-06-20T10:00:00")
    val cancelledAt: LocalDateTime?,

    @Schema(description = "환불 금액", example = "4500")
    val refundAmount: Int,

    @Schema(description = "취소한 관리자 ID", example = "admin123")
    val cancelledByAdminId: String?,

    @Schema(description = "쿠폰 사용 가능 여부", example = "true")
    val isUsable: Boolean,

    @Schema(description = "쿠폰 만료 여부", example = "false")
    val isExpired: Boolean,

    @Schema(description = "메타데이터")
    val metadata: Map<String, Any>?,

    @Schema(description = "발급 일시", example = "2024-06-01T10:00:00")
    val createdAt: LocalDateTime,

    @Schema(description = "수정 일시", example = "2024-06-01T10:00:00")
    val updatedAt: LocalDateTime
) {
    data class ProductInfo(
        @Schema(description = "상품 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        val id: UUID,

        @Schema(description = "상품명", example = "스타벅스 아메리카노")
        val name: String,

        @Schema(description = "상품 설명", example = "진한 에스프레소의 풍미")
        val description: String,

        @Schema(description = "상품 이미지 URL", example = "https://example.com/product.jpg")
        val imageUrl: String,

        @Schema(description = "상품 카테고리", example = "COFFEE")
        val category: String,

        @Schema(description = "상품 카테고리 표시명", example = "커피")
        val categoryDisplayName: String
    )
}

data class CouponSummaryResponse(
    @Schema(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "상품명", example = "스타벅스 아메리카노")
    val productName: String,

    @Schema(description = "상품 이미지 URL", example = "https://example.com/product.jpg")
    val productImageUrl: String,

    @Schema(description = "원래 상품 가격", example = "4500")
    val originalPrice: Int,

    @Schema(description = "실제 지불 금액", example = "4500")
    val paidAmount: Int,

    @Schema(description = "쿠폰 상태", example = "ACTIVE")
    val status: String,

    @Schema(description = "쿠폰 상태 표시명", example = "활성")
    val statusDisplayName: String,

    @Schema(description = "만료일시", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,

    @Schema(description = "쿠폰 사용 가능 여부", example = "true")
    val isUsable: Boolean,

    @Schema(description = "발급 일시", example = "2024-06-01T10:00:00")
    val createdAt: LocalDateTime
)

data class PurchaseCouponResponse(
    @Schema(description = "생성된 쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val couponId: UUID,

    @Schema(description = "캐시 차감 거래 ID", example = "tx_123e4567-e89b-12d3-a456-426614174000")
    val transactionId: UUID,

    @Schema(description = "차감된 캐시 금액", example = "4500")
    val deductedAmount: Int,

    @Schema(description = "새로운 캐시 잔액", example = "8000")
    val newCashBalance: Int,

    @Schema(description = "쿠폰 만료일시", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,

    @Schema(description = "구매 완료 시간", example = "2024-06-01T10:00:00")
    val purchasedAt: LocalDateTime,

    @Schema(description = "성공 메시지", example = "쿠폰이 성공적으로 구매되었습니다")
    val message: String = "쿠폰이 성공적으로 구매되었습니다"
)

data class AdminIssueCouponResponse(
    @Schema(description = "발급된 쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val couponId: UUID,

    @Schema(description = "대상 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val targetUserId: UUID,

    @Schema(description = "쿠폰 만료일시", example = "2024-12-31T23:59:59")
    val expiresAt: LocalDateTime,

    @Schema(description = "발급 완료 시간", example = "2024-06-01T10:00:00")
    val issuedAt: LocalDateTime,

    @Schema(description = "성공 메시지", example = "쿠폰이 성공적으로 발급되었습니다")
    val message: String = "쿠폰이 성공적으로 발급되었습니다"
)

data class CancelCouponResponse(
    @Schema(description = "취소된 쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val couponId: UUID,

    @Schema(description = "환불 금액", example = "4500")
    val refundAmount: Int,

    @Schema(description = "캐시 환불 거래 ID (환불이 있는 경우)", example = "tx_123e4567-e89b-12d3-a456-426614174000")
    val refundTransactionId: UUID?,

    @Schema(description = "취소 완료 시간", example = "2024-06-20T10:00:00")
    val cancelledAt: LocalDateTime,

    @Schema(description = "성공 메시지", example = "쿠폰이 성공적으로 취소되었습니다")
    val message: String = "쿠폰이 성공적으로 취소되었습니다"
)

data class UseCouponResponse(
    @Schema(description = "사용된 쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val couponId: UUID,

    @Schema(description = "사용 완료 시간", example = "2024-06-15T14:30:00")
    val usedAt: LocalDateTime,

    @Schema(description = "성공 메시지", example = "쿠폰이 성공적으로 사용되었습니다")
    val message: String = "쿠폰이 성공적으로 사용되었습니다"
)

data class CouponStatisticsResponse(
    @Schema(description = "기간", example = "2024-06")
    val period: String,

    @Schema(description = "총 발급 수", example = "1250")
    val totalIssued: Long,

    @Schema(description = "구매로 발급된 수", example = "980")
    val purchasedCount: Long,

    @Schema(description = "관리자 발급 수", example = "150")
    val adminIssuedCount: Long,

    @Schema(description = "프로모션 발급 수", example = "120")
    val promotionCount: Long,

    @Schema(description = "총 사용 수", example = "856")
    val totalUsed: Long,

    @Schema(description = "총 취소 수", example = "45")
    val totalCancelled: Long,

    @Schema(description = "총 만료 수", example = "78")
    val totalExpired: Long,

    @Schema(description = "총 매출액", example = "4410000")
    val totalRevenue: Long,

    @Schema(description = "총 환불액", example = "202500")
    val totalRefund: Long,

    @Schema(description = "사용률 (%)", example = "68.5")
    val usageRate: Double,

    @Schema(description = "취소율 (%)", example = "3.6")
    val cancellationRate: Double
)
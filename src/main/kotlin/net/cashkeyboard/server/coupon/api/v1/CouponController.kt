package net.cashkeyboard.server.coupon.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.cashkeyboard.server.coupon.application.query.CouponVerificationDto
import net.cashkeyboard.server.coupon.application.query.VerifyCouponCodeQuery
import net.cashkeyboard.server.coupon.application.query.VerifyCouponCodeQueryHandler
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon Verification API", description = "쿠폰 검증 API (외부 사용)")
class CouponController(
    private val verifyCouponCodeQueryHandler: VerifyCouponCodeQueryHandler
) {
    private val logger = LoggerFactory.getLogger(CouponController::class.java)

    @GetMapping("/{couponCode}/verify")
    @Operation(
        summary = "쿠폰 코드 검증",
        description = """
            쿠폰 코드의 유효성을 검증합니다.
            
            **검증 항목:**
            - 쿠폰 코드 존재 여부
            - 쿠폰 상태 (활성, 사용됨, 만료됨 등)
            - 만료일 확인
            - 사용 가능 여부
            
            **사용 예시:**
            - 매장 POS 시스템에서 쿠폰 코드 스캔
            - 온라인 쇼핑몰에서 쿠폰 코드 입력
            - 파트너사 시스템과의 연동
            
            **보안:**
            - 이 API는 공개 API로 인증이 필요하지 않습니다
            - 개인정보는 최소한으로만 노출됩니다
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 검증 성공",
                content = [Content(schema = Schema(implementation = CouponVerificationResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "유효하지 않은 쿠폰 코드"),
            ApiResponse(responseCode = "400", description = "잘못된 쿠폰 코드 형식")
        ]
    )
    fun verifyCouponCode(
        @Parameter(description = "검증할 쿠폰 코드", example = "GIFT123456789")
        @PathVariable couponCode: String
    ): ResponseEntity<CouponVerificationResponse> {
        logger.debug("=== verifyCouponCode called ===")
        logger.debug("Coupon Code: $couponCode")

        // Basic validation
        if (couponCode.isBlank() || couponCode.length < 8) {
            logger.warn("Invalid coupon code format: $couponCode")
            return ResponseEntity.badRequest().build()
        }

        val query = VerifyCouponCodeQuery(couponCode = couponCode)
        val verificationDto = verifyCouponCodeQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = CouponVerificationResponse(
            couponId = verificationDto.couponId,
            productName = verificationDto.productName,
            status = verificationDto.status,
            isUsable = verificationDto.isUsable,
            expiresAt = verificationDto.expiresAt,
            // ownerUserId는 보안상 노출하지 않음
            isValid = verificationDto.isUsable,
            verifiedAt = LocalDateTime.now()
        )

        return ResponseEntity.ok(response)
    }

    data class CouponVerificationResponse(
        @Schema(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        val couponId: UUID,

        @Schema(description = "상품명", example = "스타벅스 아메리카노")
        val productName: String,

        @Schema(description = "쿠폰 상태", example = "ACTIVE")
        val status: String,

        @Schema(description = "사용 가능 여부", example = "true")
        val isUsable: Boolean,

        @Schema(description = "만료일시", example = "2024-12-31T23:59:59")
        val expiresAt: LocalDateTime,

        @Schema(description = "유효한 쿠폰 여부", example = "true")
        val isValid: Boolean,

        @Schema(description = "검증 시간", example = "2024-06-08T14:30:00")
        val verifiedAt: LocalDateTime
    )
}
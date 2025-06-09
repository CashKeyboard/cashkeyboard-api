package net.cashkeyboard.server.coupon.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.cashkeyboard.server.common.security.AuthUtil
import net.cashkeyboard.server.coupon.api.dto.*
import net.cashkeyboard.server.coupon.application.command.*
import net.cashkeyboard.server.coupon.application.query.*
import net.cashkeyboard.server.coupon.domain.CouponStatus
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/coupons")
@Tag(name = "User Coupon API", description = "사용자 쿠폰 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
class UserCouponController(
    private val purchaseCouponCommandHandler: PurchaseCouponCommandHandler,
    private val useCouponCommandHandler: UseCouponCommandHandler,
    private val getCouponByIdQueryHandler: GetCouponByIdQueryHandler,
    private val getUserCouponsQueryHandler: GetUserCouponsQueryHandler
) {
    private val logger = LoggerFactory.getLogger(UserCouponController::class.java)

    @PostMapping
    @Operation(
        summary = "쿠폰 구매",
        description = """
            캐시를 사용하여 상품 쿠폰을 구매합니다.
            
            **구매 프로세스:**
            1. 상품 재고 및 구매 가능 여부 확인
            2. 사용자 캐시 잔액 확인
            3. 캐시 차감 및 쿠폰 발급
            4. 기프티콘 API 연동 (백그라운드)
            5. FCM 알림 발송
            
            **제한사항:**
            - 충분한 캐시 잔액 필요
            - 상품이 활성화 상태이고 재고가 있어야 함
            - Rate Limiting 적용
        """,
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "쿠폰 구매 성공",
                content = [Content(schema = Schema(implementation = PurchaseCouponResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 캐시 부족"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "상품 재고 부족 또는 구매 불가"),
            ApiResponse(responseCode = "429", description = "Rate Limit 초과")
        ]
    )
    fun purchaseCoupon(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @Valid @RequestBody request: PurchaseCouponRequest
    ): ResponseEntity<PurchaseCouponResponse> {
        logger.debug("=== purchaseCoupon called ===")
        logger.debug("User ID: $userId, Product ID: ${request.productId}")

        validateUserAccess(userId)

        val command = PurchaseCouponCommand(
            userId = userId,
            productId = request.productId,
            metadata = request.metadata
        )

        val result = purchaseCouponCommandHandler.handle(command)

        val response = PurchaseCouponResponse(
            couponId = result.couponId,
            transactionId = result.transactionId,
            deductedAmount = result.deductedAmount,
            newCashBalance = result.newCashBalance,
            expiresAt = result.expiresAt,
            purchasedAt = result.purchasedAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "사용자 쿠폰 목록 조회",
        description = """
            사용자의 쿠폰 목록을 조회합니다.
            
            **필터링 옵션:**
            - `status`: 쿠폰 상태 ("ACTIVE", "USED", "EXPIRED", "CANCELLED", "REFUNDED")
            - `productId`: 특정 상품의 쿠폰만 조회
            - `startDate`: 시작 날짜 (YYYY-MM-DD)
            - `endDate`: 종료 날짜 (YYYY-MM-DD)
            
            **정렬 옵션:**
            - `createdAt,desc` - 최신순 (기본값)
            - `createdAt,asc` - 오래된순
            - `expiresAt,asc` - 만료일 가까운순
            - `expiresAt,desc` - 만료일 먼순
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 목록 조회 성공",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음")
        ]
    )
    fun getUserCoupons(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,

        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable,

        @Parameter(description = "쿠폰 상태 필터")
        @RequestParam(required = false) status: String?,

        @Parameter(description = "상품 ID 필터")
        @RequestParam(required = false) productId: UUID?,

        @Parameter(description = "시작 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) startDate: String?,

        @Parameter(description = "종료 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<Page<CouponSummaryResponse>> {
        logger.debug("=== getUserCoupons called ===")
        logger.debug("User ID: $userId, Status: $status, Product ID: $productId")

        validateUserAccess(userId)

        // Parse status
        val couponStatus = status?.let {
            try {
                CouponStatus.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid status provided: $status")
                return ResponseEntity.badRequest().build()
            }
        }

        // Parse dates
        val startDateTime = startDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        }
        val endDateTime = endDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1).atStartOfDay()
        }

        val query = GetUserCouponsQuery(
            userId = userId,
            pageable = pageable,
            status = couponStatus,
            productId = productId,
            startDate = startDateTime,
            endDate = endDateTime
        )

        val coupons = getUserCouponsQueryHandler.handle(query)
        val response = coupons.map { dto ->
            CouponSummaryResponse(
                id = dto.id,
                productName = dto.productName,
                productImageUrl = dto.productImageUrl,
                originalPrice = dto.originalPrice,
                paidAmount = dto.paidAmount,
                status = dto.status,
                statusDisplayName = dto.statusDisplayName,
                expiresAt = dto.expiresAt,
                isUsable = dto.isUsable,
                createdAt = dto.createdAt
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{couponId}")
    @Operation(
        summary = "쿠폰 상세 조회",
        description = "특정 쿠폰의 상세 정보를 조회합니다. 본인의 쿠폰만 조회 가능합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 상세 조회 성공",
                content = [Content(schema = Schema(implementation = CouponResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
        ]
    )
    fun getCouponById(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,

        @Parameter(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable couponId: UUID
    ): ResponseEntity<CouponResponse> {
        logger.debug("=== getCouponById called ===")
        logger.debug("User ID: $userId, Coupon ID: $couponId")

        validateUserAccess(userId)

        val query = GetCouponByIdQuery(couponId = couponId, userId = userId)
        val couponDto = getCouponByIdQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = CouponResponse(
            id = couponDto.id,
            userId = couponDto.userId,
            product = CouponResponse.ProductInfo(
                id = couponDto.productId,
                name = couponDto.productName,
                description = couponDto.productDescription,
                imageUrl = couponDto.productImageUrl,
                category = couponDto.productCategory,
                categoryDisplayName = couponDto.productCategoryDisplayName
            ),
            originalPrice = couponDto.originalPrice,
            paidAmount = couponDto.paidAmount,
            issueType = couponDto.issueType,
            issueReason = couponDto.issueReason,
            couponCode = couponDto.couponCode,
            couponImageUrl = couponDto.couponImageUrl,
            status = couponDto.status,
            statusDisplayName = couponDto.statusDisplayName,
            expiresAt = couponDto.expiresAt,
            usedAt = couponDto.usedAt,
            cancelledAt = couponDto.cancelledAt,
            refundAmount = couponDto.refundAmount,
            cancelledByAdminId = couponDto.cancelledByAdminId,
            isUsable = couponDto.isUsable,
            isExpired = couponDto.isExpired,
            metadata = couponDto.metadata,
            createdAt = couponDto.createdAt,
            updatedAt = couponDto.updatedAt
        )

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{couponId}/use")
    @Operation(
        summary = "쿠폰 사용",
        description = """
            쿠폰을 사용 처리합니다.
            
            **사용 조건:**
            - 쿠폰이 활성 상태여야 함
            - 만료되지 않았어야 함
            - 본인의 쿠폰이어야 함
            
            **처리 프로세스:**
            1. 쿠폰 상태 검증
            2. 사용 처리
            3. 사용 로그 기록
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 사용 성공",
                content = [Content(schema = Schema(implementation = UseCouponResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "쿠폰 사용 불가 (이미 사용됨, 만료됨 등)"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
        ]
    )
    fun useCoupon(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,

        @Parameter(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable couponId: UUID,

        @Valid @RequestBody request: UseCouponRequest
    ): ResponseEntity<UseCouponResponse> {
        logger.debug("=== useCoupon called ===")
        logger.debug("User ID: $userId, Coupon ID: $couponId")

        validateUserAccess(userId)

        val command = UseCouponCommand(
            couponId = couponId,
            userId = userId,
            metadata = request.metadata
        )

        val result = useCouponCommandHandler.handle(command)

        val response = UseCouponResponse(
            couponId = result.couponId,
            usedAt = result.usedAt
        )

        return ResponseEntity.ok(response)
    }

    private fun validateUserAccess(userId: UUID) {
        if (!AuthUtil.isCurrentUser(userId)) {
            logger.warn("Access denied: User trying to access another user's coupons")
            throw AccessDeniedException("You can only access your own coupons")
        }
    }
}
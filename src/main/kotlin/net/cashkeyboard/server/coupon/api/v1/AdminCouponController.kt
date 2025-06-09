package net.cashkeyboard.server.coupon.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.cashkeyboard.server.coupon.api.dto.*
import net.cashkeyboard.server.coupon.application.command.*
import net.cashkeyboard.server.coupon.application.query.*
import net.cashkeyboard.server.coupon.domain.CouponIssueType
import net.cashkeyboard.server.coupon.domain.CouponStatus
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/v1/admin/coupons")
@Tag(name = "Admin Coupon API", description = "관리자 쿠폰 관리 API")
@SecurityRequirement(name = "Admin Key")
class AdminCouponController(
    private val adminIssueCouponCommandHandler: AdminIssueCouponCommandHandler,
    private val cancelCouponCommandHandler: CancelCouponCommandHandler,
    private val getCouponByIdQueryHandler: GetCouponByIdQueryHandler,
    private val getCouponsForAdminQueryHandler: GetCouponsForAdminQueryHandler,
    private val getCouponStatisticsQueryHandler: GetCouponStatisticsQueryHandler
) {
    private val logger = LoggerFactory.getLogger(AdminCouponController::class.java)

    @PostMapping("/issue")
    @Operation(
        summary = "관리자 쿠폰 발급",
        description = """
            관리자가 특정 사용자에게 직접 쿠폰을 발급합니다.
            
            **발급 프로세스:**
            1. 대상 사용자 및 상품 검증
            2. 무료 쿠폰 발급 (캐시 차감 없음)
            3. 기프티콘 API 연동 (백그라운드)
            4. FCM 알림 발송
            
            **용도:**
            - 고객 서비스 보상
            - 프로모션 이벤트
            - 시스템 오류 보상
            - 특별 혜택 제공
        """,
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "쿠폰 발급 성공",
                content = [Content(schema = Schema(implementation = AdminIssueCouponResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "Admin 인증 필요"),
            ApiResponse(responseCode = "404", description = "사용자 또는 상품을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "상품이 비활성 상태")
        ]
    )
    fun adminIssueCoupon(
        @Valid @RequestBody request: AdminIssueCouponRequest
    ): ResponseEntity<AdminIssueCouponResponse> {
        logger.debug("=== adminIssueCoupon called ===")
        logger.debug("Target User ID: ${request.targetUserId}, Product ID: ${request.productId}")

        val command = AdminIssueCouponCommand(
            adminId = "admin", // TODO: Get from security context
            targetUserId = request.targetUserId,
            productId = request.productId,
            issueReason = request.issueReason,
            expiresAt = request.expiresAt,
            metadata = request.metadata
        )

        val result = adminIssueCouponCommandHandler.handle(command)

        val response = AdminIssueCouponResponse(
            couponId = result.couponId,
            targetUserId = result.targetUserId,
            expiresAt = result.expiresAt,
            issuedAt = result.issuedAt
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "관리자 쿠폰 목록 조회",
        description = """
            관리자용 쿠폰 목록을 조회합니다.
            
            **필터링 옵션:**
            - `userId`: 특정 사용자의 쿠폰만 조회
            - `productId`: 특정 상품의 쿠폰만 조회
            - `status`: 쿠폰 상태 필터
            - `issueType`: 발급 타입 필터
            - `startDate`: 시작 날짜 (YYYY-MM-DD)
            - `endDate`: 종료 날짜 (YYYY-MM-DD)
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 목록 조회 성공",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(responseCode = "401", description = "Admin 인증 필요")
        ]
    )
    fun getCouponsForAdmin(
        @PageableDefault(
            size = 50,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable,

        @Parameter(description = "사용자 ID 필터")
        @RequestParam(required = false) userId: UUID?,

        @Parameter(description = "상품 ID 필터")
        @RequestParam(required = false) productId: UUID?,

        @Parameter(description = "쿠폰 상태 필터")
        @RequestParam(required = false) status: String?,

        @Parameter(description = "발급 타입 필터")
        @RequestParam(required = false) issueType: String?,

        @Parameter(description = "시작 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) startDate: String?,

        @Parameter(description = "종료 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<Page<CouponResponse>> {
        logger.debug("=== getCouponsForAdmin called ===")
        logger.debug("Filters - userId: $userId, productId: $productId, status: $status, issueType: $issueType")

        // Parse status
        val couponStatus = status?.let {
            try {
                CouponStatus.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid status provided: $status")
                return ResponseEntity.badRequest().build()
            }
        }

        // Parse issue type
        val couponIssueType = issueType?.let {
            try {
                CouponIssueType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid issue type provided: $issueType")
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

        val query = GetCouponsForAdminQuery(
            pageable = pageable,
            userId = userId,
            productId = productId,
            status = couponStatus,
            issueType = couponIssueType,
            startDate = startDateTime,
            endDate = endDateTime
        )

        val coupons = getCouponsForAdminQueryHandler.handle(query)
        val response = coupons.map { dto ->
            CouponResponse(
                id = dto.id,
                userId = dto.userId,
                product = CouponResponse.ProductInfo(
                    id = dto.productId,
                    name = dto.productName,
                    description = dto.productDescription,
                    imageUrl = dto.productImageUrl,
                    category = dto.productCategory,
                    categoryDisplayName = dto.productCategoryDisplayName
                ),
                originalPrice = dto.originalPrice,
                paidAmount = dto.paidAmount,
                issueType = dto.issueType,
                issueReason = dto.issueReason,
                couponCode = dto.couponCode,
                couponImageUrl = dto.couponImageUrl,
                status = dto.status,
                statusDisplayName = dto.statusDisplayName,
                expiresAt = dto.expiresAt,
                usedAt = dto.usedAt,
                cancelledAt = dto.cancelledAt,
                refundAmount = dto.refundAmount,
                cancelledByAdminId = dto.cancelledByAdminId,
                isUsable = dto.isUsable,
                isExpired = dto.isExpired,
                metadata = dto.metadata,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{couponId}")
    @Operation(
        summary = "관리자 쿠폰 상세 조회",
        description = "관리자가 특정 쿠폰의 상세 정보를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 상세 조회 성공",
                content = [Content(schema = Schema(implementation = CouponResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Admin 인증 필요"),
            ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
        ]
    )
    fun getCouponByIdForAdmin(
        @Parameter(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable couponId: UUID
    ): ResponseEntity<CouponResponse> {
        logger.debug("=== getCouponByIdForAdmin called ===")
        logger.debug("Coupon ID: $couponId")

        val query = GetCouponByIdQuery(couponId = couponId) // No user access control for admin
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

    @PutMapping("/{couponId}/cancel")
    @Operation(
        summary = "쿠폰 취소/환불",
        description = """
            관리자가 쿠폰을 취소하고 필요시 환불 처리합니다.
            
            **취소 프로세스:**
            1. 쿠폰 취소 가능 여부 확인
            2. 쿠폰 상태를 CANCELLED로 변경
            3. 환불 금액이 있는 경우 캐시 환불 처리
            4. 취소 로그 기록
            
            **환불 규칙:**
            - 구매 쿠폰: 지불한 금액 내에서 환불 가능
            - 무료 발급 쿠폰: 환불 금액 0
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "쿠폰 취소 성공",
                content = [Content(schema = Schema(implementation = CancelCouponResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "취소 불가능한 쿠폰 또는 잘못된 환불 금액"),
            ApiResponse(responseCode = "401", description = "Admin 인증 필요"),
            ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
        ]
    )
    fun cancelCoupon(
        @Parameter(description = "쿠폰 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable couponId: UUID,

        @Valid @RequestBody request: CancelCouponRequest
    ): ResponseEntity<CancelCouponResponse> {
        logger.debug("=== cancelCoupon called ===")
        logger.debug("Coupon ID: $couponId, Refund Amount: ${request.refundAmount}")

        val command = CancelCouponCommand(
            couponId = couponId,
            adminId = "admin", // TODO: Get from security context
            reason = request.reason,
            refundAmount = request.refundAmount,
            metadata = request.metadata
        )

        val result = cancelCouponCommandHandler.handle(command)

        val response = CancelCouponResponse(
            couponId = result.couponId,
            refundAmount = result.refundAmount,
            refundTransactionId = result.refundTransactionId,
            cancelledAt = result.cancelledAt
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "쿠폰 발급 통계",
        description = """
            쿠폰 발급 및 사용에 대한 통계 정보를 조회합니다.
            
            **통계 항목:**
            - 총 발급 수량 (발급 타입별)
            - 총 사용 수량
            - 총 취소/환불 수량
            - 매출액 및 환불액
            - 사용률 및 취소율
            
            **기간 옵션:**
            - 일별, 주별, 월별, 연별 그룹핑 가능
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "통계 조회 성공",
                content = [Content(schema = Schema(implementation = CouponStatisticsResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Admin 인증 필요"),
            ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
        ]
    )
    fun getCouponStatistics(
        @Parameter(description = "시작 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        @RequestParam startDate: String,

        @Parameter(description = "종료 날짜 (YYYY-MM-DD)", example = "2024-12-31")
        @RequestParam endDate: String,

        @Parameter(description = "그룹핑 단위 (DAY, WEEK, MONTH, YEAR)", example = "MONTH")
        @RequestParam(defaultValue = "MONTH") groupBy: String
    ): ResponseEntity<CouponStatisticsResponse> {
        logger.debug("=== getCouponStatistics called ===")
        logger.debug("Period: $startDate ~ $endDate, Group by: $groupBy")

        // Parse dates
        val startDateTime = try {
            LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        } catch (e: Exception) {
            logger.warn("Invalid start date format: $startDate")
            return ResponseEntity.badRequest().build()
        }

        val endDateTime = try {
            LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1).atStartOfDay()
        } catch (e: Exception) {
            logger.warn("Invalid end date format: $endDate")
            return ResponseEntity.badRequest().build()
        }

        // Parse group by
        val groupByEnum = try {
            GetCouponStatisticsQuery.StatisticsGroupBy.valueOf(groupBy.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid group by option: $groupBy")
            return ResponseEntity.badRequest().build()
        }

        val query = GetCouponStatisticsQuery(
            startDate = startDateTime,
            endDate = endDateTime,
            groupBy = groupByEnum
        )

        val statisticsDto = getCouponStatisticsQueryHandler.handle(query)

        val response = CouponStatisticsResponse(
            period = statisticsDto.period,
            totalIssued = statisticsDto.totalIssued,
            purchasedCount = statisticsDto.purchasedCount,
            adminIssuedCount = statisticsDto.adminIssuedCount,
            promotionCount = statisticsDto.promotionCount,
            totalUsed = statisticsDto.totalUsed,
            totalCancelled = statisticsDto.totalCancelled,
            totalExpired = statisticsDto.totalExpired,
            totalRevenue = statisticsDto.totalRevenue,
            totalRefund = statisticsDto.totalRefund,
            usageRate = statisticsDto.usageRate,
            cancellationRate = statisticsDto.cancellationRate
        )

        return ResponseEntity.ok(response)
    }
}
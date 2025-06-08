package net.cashkeyboard.server.cash.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.cashkeyboard.server.cash.api.dto.*
import net.cashkeyboard.server.cash.application.command.*
import net.cashkeyboard.server.cash.application.query.*
import net.cashkeyboard.server.cash.domain.EarnSource
import net.cashkeyboard.server.cash.domain.SpendPurpose
import net.cashkeyboard.server.cash.domain.TransactionType
import net.cashkeyboard.server.common.security.AuthUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/cash")
@Tag(name = "Cash API", description = "사용자 캐시 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
class CashController(
    private val earnCashCommandHandler: EarnCashCommandHandler,
    private val randomEarnCashCommandHandler: RandomEarnCashCommandHandler,
    private val spendCashCommandHandler: SpendCashCommandHandler,
    private val getCashAccountQueryHandler: GetCashAccountQueryHandler,
    private val getCashTransactionsQueryHandler: GetCashTransactionsQueryHandler,
    private val getDailyLimitsQueryHandler: GetDailyLimitsQueryHandler
) {
    private val logger = LoggerFactory.getLogger(CashController::class.java)

    @GetMapping
    @Operation(
        summary = "캐시 계정 조회",
        description = "사용자의 캐시 잔액, 통계, 일일 한도 정보를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "캐시 계정 조회 성공",
                content = [Content(schema = Schema(implementation = CashAccountResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "캐시 계정을 찾을 수 없음")
        ]
    )
    fun getCashAccount(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID
    ): ResponseEntity<CashAccountResponse> {
        logger.debug("=== getCashAccount called ===")
        logger.debug("Requested user ID: $userId")

        validateUserAccess(userId)

        val query = GetCashAccountQuery(userId)
        val cashAccountDto = getCashAccountQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = CashAccountResponse(
            userId = cashAccountDto.userId,
            balance = cashAccountDto.balance,
            todayEarned = cashAccountDto.todayEarned,
            todaySpent = cashAccountDto.todaySpent,
            totalEarned = cashAccountDto.totalEarned,
            totalSpent = cashAccountDto.totalSpent,
            limits = CashAccountResponse.LimitsInfo(
                maxDailyEarn = cashAccountDto.dailyLimits.maxDailyEarn,
                todayEarnedCount = cashAccountDto.dailyLimits.todayEarnedCount,
                maxRandomEarnCount = cashAccountDto.dailyLimits.maxRandomEarnCount,
                todayRandomEarnedCount = cashAccountDto.dailyLimits.todayRandomEarnedCount,
                remainingEarnLimit = cashAccountDto.dailyLimits.remainingEarnLimit,
                remainingRandomEarnCount = cashAccountDto.dailyLimits.remainingRandomEarnCount
            ),
            lastEarnedAt = cashAccountDto.lastEarnedAt,
            createdAt = cashAccountDto.createdAt,
            updatedAt = cashAccountDto.updatedAt
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/earnings")
    @Operation(
        summary = "일반 캐시 적립",
        description = """
            광고 시청, 미션 완료 등을 통한 일반 캐시 적립을 처리합니다.
            
            **제한사항:**
            - 일일 최대 적립 한도: 1,000 캐시
            - 일일 최대 적립 횟수: 20회
            - Rate Limiting: 1분당 최대 요청 제한
            - 동일한 sourceId로 중복 적립 불가
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "캐시 적립 성공",
                content = [Content(schema = Schema(implementation = EarnCashResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "409", description = "중복된 소스 ID"),
            ApiResponse(responseCode = "429", description = "일일 한도 초과 또는 Rate Limit 초과")
        ]
    )
    fun earnCash(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @Valid @RequestBody request: EarnCashRequest
    ): ResponseEntity<EarnCashResponse> {
        logger.debug("=== earnCash called ===")
        logger.debug("User ID: $userId, Amount: ${request.amount}, Source: ${request.source}")

        validateUserAccess(userId)

        val command = EarnCashCommand(
            userId = userId,
            amount = request.amount,
            source = EarnSource.valueOf(request.source),
            sourceId = request.sourceId,
            metadata = request.metadata
        )

        val result = earnCashCommandHandler.handle(command)

        val response = EarnCashResponse(
            transactionId = result.transactionId,
            earnedAmount = result.earnedAmount,
            newBalance = result.newBalance,
            limits = EarnCashResponse.DailyLimitsStatus(
                todayEarned = result.dailyStatus.todayEarned,
                remainingLimit = result.dailyStatus.remainingLimit,
                todayEarnedCount = result.dailyStatus.todayEarnedCount,
                remainingRandomEarnCount = result.dailyStatus.remainingRandomEarnCount
            ),
            timestamp = result.timestamp
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/random-earnings")
    @Operation(
        summary = "랜덤 캐시 적립",
        description = """
            확률 기반 랜덤 캐시 적립을 처리합니다. (룰렛, 복권 등)
            
            **제한사항:**
            - 일일 최대 시도 횟수: 10회
            - Rate Limiting: 1분당 최대 요청 제한
            - 동일한 sourceId로 중복 시도 불가
            - 서버 시간 기준으로 처리 (시간 위조 방지)
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "랜덤 캐시 적립 처리 완료",
                content = [Content(schema = Schema(implementation = RandomEarnCashResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "409", description = "중복된 소스 ID"),
            ApiResponse(responseCode = "429", description = "일일 시도 한도 초과 또는 Rate Limit 초과")
        ]
    )
    fun earnRandomCash(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @Valid @RequestBody request: RandomEarnCashRequest
    ): ResponseEntity<RandomEarnCashResponse> {
        logger.debug("=== earnRandomCash called ===")
        logger.debug("User ID: $userId, Source: ${request.source}")

        validateUserAccess(userId)

        val command = RandomEarnCashCommand(
            userId = userId,
            source = EarnSource.valueOf(request.source),
            sourceId = request.sourceId,
            metadata = request.metadata
        )

        val result = randomEarnCashCommandHandler.handle(command)

        val response = RandomEarnCashResponse(
            transactionId = result.transactionId,
            isWinner = result.isWinner,
            earnedAmount = result.earnedAmount,
            newBalance = result.newBalance,
            probability = RandomEarnCashResponse.ProbabilityInfo(
                winRate = result.randomResult.winRate,
                tier = result.randomResult.tier,
                possibleAmounts = result.randomResult.possibleAmounts
            ),
            limits = RandomEarnCashResponse.RandomEarnLimitsStatus(
                todayRandomEarnedCount = result.dailyStatus.todayRandomEarnedCount,
                remainingRandomEarnCount = result.dailyStatus.remainingRandomEarnCount
            ),
            timestamp = result.timestamp
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/spendings")
    @Operation(
        summary = "캐시 사용",
        description = """
            상품 구매, 프리미엄 기능 이용 등을 위한 캐시 사용을 처리합니다.
            
            **제한사항:**
            - 보유 잔액 내에서만 사용 가능
            - Rate Limiting: 1분당 최대 요청 제한
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "캐시 사용 성공",
                content = [Content(schema = Schema(implementation = SpendCashResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 잔액 부족"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "429", description = "Rate Limit 초과")
        ]
    )
    fun spendCash(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @Valid @RequestBody request: SpendCashRequest
    ): ResponseEntity<SpendCashResponse> {
        logger.debug("=== spendCash called ===")
        logger.debug("User ID: $userId, Amount: ${request.amount}, Purpose: ${request.purpose}")

        validateUserAccess(userId)

        val command = SpendCashCommand(
            userId = userId,
            amount = request.amount,
            purpose = SpendPurpose.valueOf(request.purpose),
            targetId = request.targetId,
            metadata = request.metadata
        )

        val result = spendCashCommandHandler.handle(command)

        val response = SpendCashResponse(
            transactionId = result.transactionId,
            spentAmount = result.spentAmount,
            newBalance = result.newBalance,
            timestamp = result.timestamp
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/transactions")
    @Operation(
        summary = "캐시 거래 내역 조회",
        description = """
            사용자의 캐시 적립/사용 내역을 조회합니다.
            
            **필터링 옵션:**
            - `type`: 거래 타입 ("earnings", "random-earnings", "spendings")
            - `source`: 적립 소스 (AD_WATCH, MISSION_COMPLETE 등)
            - `startDate`: 시작 날짜 (YYYY-MM-DD)
            - `endDate`: 종료 날짜 (YYYY-MM-DD)
            
            **정렬 옵션:**
            - `createdAt,desc` - 최신순 (기본값)
            - `createdAt,asc` - 오래된순
            - `amount,desc` - 금액 높은순
            - `amount,asc` - 금액 낮은순
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "거래 내역 조회 성공",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음")
        ]
    )
    fun getTransactions(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,

        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable,

        @Parameter(description = "거래 타입 필터")
        @RequestParam(required = false) type: String?,

        @Parameter(description = "적립 소스 필터")
        @RequestParam(required = false) source: String?,

        @Parameter(description = "시작 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) startDate: String?,

        @Parameter(description = "종료 날짜 (YYYY-MM-DD)")
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<Page<TransactionResponse>> {
        logger.debug("=== getTransactions called ===")
        logger.debug("User ID: $userId, Type: $type, Source: $source")

        validateUserAccess(userId)

        // 타입 변환
        val transactionType = type?.let {
            when (it.lowercase()) {
                "earnings" -> TransactionType.EARN
                "random-earnings" -> TransactionType.RANDOM_EARN
                "spendings" -> TransactionType.SPEND
                else -> null
            }
        }

        val earnSource = source?.let {
            try {
                EarnSource.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        // 날짜 파싱
        val startDateTime = startDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        }
        val endDateTime = endDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1).atStartOfDay()
        }

        val query = GetCashTransactionsQuery(
            userId = userId,
            pageable = pageable,
            type = transactionType,
            source = earnSource,
            startDate = startDateTime,
            endDate = endDateTime
        )

        val transactions = getCashTransactionsQueryHandler.handle(query)
        val response = transactions.map { dto ->
            TransactionResponse(
                id = dto.id,
                type = when (dto.type) {
                    TransactionType.EARN -> "earnings"
                    TransactionType.RANDOM_EARN -> "random-earnings"
                    TransactionType.SPEND -> "spendings"
                },
                amount = dto.amount,
                source = dto.source?.name,
                sourceId = dto.sourceId,
                purpose = dto.purpose,
                targetId = dto.targetId,
                balanceAfter = dto.balanceAfter,
                metadata = dto.metadata,
                timestamp = dto.timestamp
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/limits")
    @Operation(
        summary = "일일 한도 조회",
        description = "사용자의 현재 일일 한도 상태를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "일일 한도 조회 성공",
                content = [Content(schema = Schema(implementation = LimitsResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음")
        ]
    )
    fun getLimits(
        @Parameter(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID
    ): ResponseEntity<LimitsResponse> {
        logger.debug("=== getLimits called ===")
        logger.debug("User ID: $userId")

        validateUserAccess(userId)

        val today = LocalDate.now()
        val query = GetDailyLimitsQuery(userId = userId, date = today)
        val limitsDto = getDailyLimitsQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = LimitsResponse(
            userId = userId,
            date = today.toString(),
            earnLimits = LimitsResponse.EarnLimitsInfo(
                maxDailyEarn = limitsDto.maxDailyEarn,
                todayEarned = limitsDto.todayEarned,
                remainingLimit = limitsDto.remainingEarnLimit,
                maxDailyEarnCount = 20, // DailyLimit.MAX_DAILY_EARN_COUNT
                todayEarnedCount = limitsDto.todayEarnedCount,
                remainingEarnCount = limitsDto.remainingEarnCount
            ),
            randomEarnLimits = LimitsResponse.RandomEarnLimitsInfo(
                maxDailyRandomEarn = limitsDto.maxRandomEarnCount,
                todayRandomEarnedCount = limitsDto.todayRandomEarnedCount,
                remainingRandomEarnCount = limitsDto.remainingRandomEarnCount
            ),
            resetTime = today.plusDays(1).atStartOfDay(),
            timezone = "Asia/Seoul"
        )

        return ResponseEntity.ok(response)
    }

    private fun validateUserAccess(userId: UUID) {
        if (!AuthUtil.isCurrentUser(userId)) {
            logger.warn("Access denied: User trying to access another user's cash account")
            throw AccessDeniedException("You can only access your own cash account")
        }
    }
}
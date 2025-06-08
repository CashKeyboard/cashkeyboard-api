package net.cashkeyboard.server.cash.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

data class CashAccountResponse(
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val userId: UUID,

    @Schema(description = "현재 잔액", example = "12500")
    val balance: Int,

    @Schema(description = "오늘 적립한 캐시", example = "500")
    val todayEarned: Int,

    @Schema(description = "오늘 사용한 캐시", example = "200")
    val todaySpent: Int,

    @Schema(description = "총 적립한 캐시", example = "45000")
    val totalEarned: Int,

    @Schema(description = "총 사용한 캐시", example = "32500")
    val totalSpent: Int,

    @Schema(description = "일일 한도 정보")
    val limits: LimitsInfo,

    @Schema(description = "마지막 적립 시간", example = "2024-01-15T10:30:00")
    val lastEarnedAt: LocalDateTime?,

    @Schema(description = "계정 생성 시간", example = "2024-01-01T00:00:00")
    val createdAt: LocalDateTime,

    @Schema(description = "계정 수정 시간", example = "2024-01-15T10:30:00")
    val updatedAt: LocalDateTime
) {
    data class LimitsInfo(
        @Schema(description = "일일 최대 적립 가능 금액", example = "1000")
        val maxDailyEarn: Int,

        @Schema(description = "오늘 적립 횟수", example = "8")
        val todayEarnedCount: Int,

        @Schema(description = "일일 최대 랜덤 적립 횟수", example = "10")
        val maxRandomEarnCount: Int,

        @Schema(description = "오늘 랜덤 적립 횟수", example = "3")
        val todayRandomEarnedCount: Int,

        @Schema(description = "남은 적립 한도", example = "500")
        val remainingEarnLimit: Int,

        @Schema(description = "남은 랜덤 적립 횟수", example = "7")
        val remainingRandomEarnCount: Int
    )
}

data class EarnCashResponse(
    @Schema(description = "거래 ID", example = "tx_123e4567-e89b-12d3-a456-426614174000")
    val transactionId: UUID,

    @Schema(description = "적립된 캐시 금액", example = "50")
    val earnedAmount: Int,

    @Schema(description = "새로운 잔액", example = "12550")
    val newBalance: Int,

    @Schema(description = "일일 한도 상태")
    val limits: DailyLimitsStatus,

    @Schema(description = "적립 시간", example = "2024-01-15T10:35:00")
    val timestamp: LocalDateTime
) {
    data class DailyLimitsStatus(
        @Schema(description = "오늘 적립한 총 금액", example = "550")
        val todayEarned: Int,

        @Schema(description = "남은 적립 한도", example = "450")
        val remainingLimit: Int,

        @Schema(description = "오늘 적립 횟수", example = "9")
        val todayEarnedCount: Int,

        @Schema(description = "남은 랜덤 적립 횟수", example = "7")
        val remainingRandomEarnCount: Int
    )
}

data class RandomEarnCashResponse(
    @Schema(description = "거래 ID (당첨 시에만)", example = "tx_789e4567-e89b-12d3-a456-426614174000")
    val transactionId: UUID?,

    @Schema(description = "당첨 여부", example = "true")
    val isWinner: Boolean,

    @Schema(description = "적립된 캐시 금액", example = "150")
    val earnedAmount: Int,

    @Schema(description = "새로운 잔액", example = "12700")
    val newBalance: Int,

    @Schema(description = "확률 정보")
    val probability: ProbabilityInfo,

    @Schema(description = "일일 한도 상태")
    val limits: RandomEarnLimitsStatus,

    @Schema(description = "처리 시간", example = "2024-01-15T10:40:00")
    val timestamp: LocalDateTime
) {
    data class ProbabilityInfo(
        @Schema(description = "당첨 확률", example = "0.3")
        val winRate: Double,

        @Schema(description = "당첨 등급", example = "RARE")
        val tier: String,

        @Schema(description = "가능한 당첨 금액 목록", example = "[50, 100, 150, 200, 500]")
        val possibleAmounts: List<Int>
    )

    data class RandomEarnLimitsStatus(
        @Schema(description = "오늘 랜덤 적립 횟수", example = "4")
        val todayRandomEarnedCount: Int,

        @Schema(description = "남은 랜덤 적립 횟수", example = "6")
        val remainingRandomEarnCount: Int
    )
}

data class SpendCashResponse(
    @Schema(description = "거래 ID", example = "tx_456e4567-e89b-12d3-a456-426614174000")
    val transactionId: UUID,

    @Schema(description = "사용된 캐시 금액", example = "500")
    val spentAmount: Int,

    @Schema(description = "새로운 잔액", example = "12000")
    val newBalance: Int,

    @Schema(description = "사용 시간", example = "2024-01-15T10:45:00")
    val timestamp: LocalDateTime
)

data class TransactionResponse(
    @Schema(description = "거래 ID", example = "tx_789e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "거래 타입", example = "random-earnings")
    val type: String,

    @Schema(description = "금액", example = "150")
    val amount: Int,

    @Schema(description = "적립 소스", example = "LUCKY_SPIN")
    val source: String?,

    @Schema(description = "소스 ID", example = "spin_67890")
    val sourceId: String?,

    @Schema(description = "사용 목적", example = "PRODUCT_PURCHASE")
    val purpose: String?,

    @Schema(description = "대상 ID", example = "product_123")
    val targetId: String?,

    @Schema(description = "거래 후 잔액", example = "12700")
    val balanceAfter: Int,

    @Schema(description = "메타데이터")
    val metadata: Map<String, Any>?,

    @Schema(description = "거래 시간", example = "2024-01-15T10:40:00")
    val timestamp: LocalDateTime
)

data class LimitsResponse(
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val userId: UUID,

    @Schema(description = "기준 날짜", example = "2024-01-15")
    val date: String,

    @Schema(description = "적립 한도 정보")
    val earnLimits: EarnLimitsInfo,

    @Schema(description = "랜덤 적립 한도 정보")
    val randomEarnLimits: RandomEarnLimitsInfo,

    @Schema(description = "한도 리셋 시간", example = "2024-01-16T00:00:00")
    val resetTime: LocalDateTime,

    @Schema(description = "시간대", example = "Asia/Seoul")
    val timezone: String
) {
    data class EarnLimitsInfo(
        @Schema(description = "일일 최대 적립 금액", example = "1000")
        val maxDailyEarn: Int,

        @Schema(description = "오늘 적립한 금액", example = "700")
        val todayEarned: Int,

        @Schema(description = "남은 적립 한도", example = "300")
        val remainingLimit: Int,

        @Schema(description = "일일 최대 적립 횟수", example = "20")
        val maxDailyEarnCount: Int,

        @Schema(description = "오늘 적립 횟수", example = "14")
        val todayEarnedCount: Int,

        @Schema(description = "남은 적립 횟수", example = "6")
        val remainingEarnCount: Int
    )

    data class RandomEarnLimitsInfo(
        @Schema(description = "일일 최대 랜덤 적립 횟수", example = "10")
        val maxDailyRandomEarn: Int,

        @Schema(description = "오늘 랜덤 적립 횟수", example = "4")
        val todayRandomEarnedCount: Int,

        @Schema(description = "남은 랜덤 적립 횟수", example = "6")
        val remainingRandomEarnCount: Int
    )
}
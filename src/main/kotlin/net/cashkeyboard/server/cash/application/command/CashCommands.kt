package net.cashkeyboard.server.cash.application.command

import net.cashkeyboard.server.cash.domain.EarnSource
import net.cashkeyboard.server.cash.domain.SpendPurpose
import java.time.LocalDateTime
import java.util.*

data class EarnCashCommand(
    val userId: UUID,
    val amount: Int,
    val source: EarnSource,
    val sourceId: String,
    val metadata: Map<String, Any>? = null
)

data class RandomEarnCashCommand(
    val userId: UUID,
    val source: EarnSource,
    val sourceId: String,
    val metadata: Map<String, Any>? = null
)

data class SpendCashCommand(
    val userId: UUID,
    val amount: Int,
    val purpose: SpendPurpose,
    val targetId: String,
    val metadata: Map<String, Any>? = null
)

data class CreateCashAccountCommand(
    val userId: UUID
)

data class EarnCashResult(
    val transactionId: UUID,
    val earnedAmount: Int,
    val newBalance: Int,
    val dailyStatus: DailyStatus,
    val timestamp: LocalDateTime
)

data class RandomEarnCashResult(
    val transactionId: UUID?,
    val isWinner: Boolean,
    val earnedAmount: Int,
    val newBalance: Int,
    val randomResult: RandomEarnResult,
    val dailyStatus: RandomEarnDailyStatus,
    val timestamp: LocalDateTime
) {
    data class RandomEarnResult(
        val winRate: Double,
        val tier: String,
        val possibleAmounts: List<Int>
    )
}

data class SpendCashResult(
    val transactionId: UUID,
    val spentAmount: Int,
    val newBalance: Int,
    val timestamp: LocalDateTime
)

data class CreateCashAccountResult(
    val accountId: UUID,
    val userId: UUID
)

data class DailyStatus(
    val todayEarned: Int,
    val remainingLimit: Int,
    val todayEarnedCount: Int,
    val remainingRandomEarnCount: Int
)

data class RandomEarnDailyStatus(
    val todayRandomEarnedCount: Int,
    val remainingRandomEarnCount: Int
)

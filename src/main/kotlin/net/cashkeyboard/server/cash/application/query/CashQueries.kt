package net.cashkeyboard.server.cash.application.query

import net.cashkeyboard.server.cash.domain.EarnSource
import net.cashkeyboard.server.cash.domain.TransactionType
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class GetCashAccountQuery(
    val userId: UUID
)

data class GetCashTransactionsQuery(
    val userId: UUID,
    val pageable: Pageable,
    val type: TransactionType? = null,
    val source: EarnSource? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class GetDailyLimitsQuery(
    val userId: UUID,
    val date: LocalDate = LocalDate.now()
)

data class CashAccountDto(
    val userId: UUID,
    val balance: Int,
    val todayEarned: Int,
    val todaySpent: Int,
    val totalEarned: Int,
    val totalSpent: Int,
    val dailyLimits: DailyLimitsDto,
    val lastEarnedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CashTransactionDto(
    val id: UUID,
    val type: TransactionType,
    val amount: Int,
    val source: EarnSource?,
    val sourceId: String?,
    val purpose: String?,
    val targetId: String?,
    val balanceAfter: Int,
    val metadata: Map<String, Any>?,
    val timestamp: LocalDateTime
)

data class DailyLimitsDto(
    val userId: UUID,
    val date: LocalDate,
    val maxDailyEarn: Int,
    val todayEarnedCount: Int,
    val maxRandomEarnCount: Int,
    val todayRandomEarnedCount: Int,
    val remainingEarnLimit: Int,
    val remainingRandomEarnCount: Int,
    val todayEarned: Int,
    val remainingEarnCount: Int
)

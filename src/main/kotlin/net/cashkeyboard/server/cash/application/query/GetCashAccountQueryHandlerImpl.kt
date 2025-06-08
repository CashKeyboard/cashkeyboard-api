package net.cashkeyboard.server.cash.application.query

import net.cashkeyboard.server.cash.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class GetCashAccountQueryHandlerImpl(
    private val cashAccountRepository: CashAccountRepository,
    private val cashTransactionRepository: CashTransactionRepository,
    private val dailyLimitRepository: DailyLimitRepository
) : GetCashAccountQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetCashAccountQuery): CashAccountDto? {
        val cashAccount = cashAccountRepository.findByUserId(query.userId)
            .orElse(null) ?: return null

        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        val todayEarned = cashTransactionRepository.sumAmountByUserAndTypesAndDateRange(
            userId = query.userId,
            types = listOf(TransactionType.EARN, TransactionType.RANDOM_EARN),
            startDate = startOfDay,
            endDate = endOfDay
        )

        // 오늘 사용한 캐시 계산
        val todaySpent = cashTransactionRepository.sumAmountByUserAndTypesAndDateRange(
            userId = query.userId,
            types = listOf(TransactionType.SPEND),
            startDate = startOfDay,
            endDate = endOfDay
        )

        // 일일 한도 정보
        val dailyLimit = dailyLimitRepository.findByUserIdAndDate(query.userId, today)
            .orElseGet {
                DailyLimit(
                    userId = query.userId,
                    date = today
                )
            }

        return CashAccountDto(
            userId = cashAccount.userId,
            balance = cashAccount.balance,
            todayEarned = todayEarned,
            todaySpent = todaySpent,
            totalEarned = cashAccount.totalEarned,
            totalSpent = cashAccount.totalSpent,
            dailyLimits = DailyLimitsDto(
                userId = query.userId,
                date = today,
                maxDailyEarn = DailyLimit.MAX_DAILY_EARN,
                todayEarnedCount = dailyLimit.todayEarnedCount,
                maxRandomEarnCount = DailyLimit.MAX_RANDOM_EARN_COUNT,
                todayRandomEarnedCount = dailyLimit.todayRandomEarnedCount,
                remainingEarnLimit = dailyLimit.getRemainingEarnLimit(),
                remainingRandomEarnCount = dailyLimit.getRemainingRandomEarnCount(),
                todayEarned = dailyLimit.todayEarned,
                remainingEarnCount = dailyLimit.getRemainingEarnCount()
            ),
            lastEarnedAt = cashAccount.lastEarnedAt,
            createdAt = cashAccount.createdAt,
            updatedAt = cashAccount.updatedAt
        )
    }
}

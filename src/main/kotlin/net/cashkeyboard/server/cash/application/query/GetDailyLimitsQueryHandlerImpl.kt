
// src/main/kotlin/net/cashkeyboard/server/cash/application/query/GetDailyLimitsQueryHandlerImpl.kt
package net.cashkeyboard.server.cash.application.query

import net.cashkeyboard.server.cash.domain.DailyLimit
import net.cashkeyboard.server.cash.domain.DailyLimitRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetDailyLimitsQueryHandlerImpl(
    private val dailyLimitRepository: DailyLimitRepository
) : GetDailyLimitsQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetDailyLimitsQuery): DailyLimitsDto? {
        val dailyLimit = dailyLimitRepository.findByUserIdAndDate(query.userId, query.date)
            .orElseGet {
                // 오늘 데이터가 없으면 기본값으로 생성 (실제로 저장하지는 않음)
                DailyLimit(
                    userId = query.userId,
                    date = query.date
                )
            }

        return DailyLimitsDto(
            userId = query.userId,
            date = query.date,
            maxDailyEarn = DailyLimit.MAX_DAILY_EARN,
            todayEarnedCount = dailyLimit.todayEarnedCount,
            maxRandomEarnCount = DailyLimit.MAX_RANDOM_EARN_COUNT,
            todayRandomEarnedCount = dailyLimit.todayRandomEarnedCount,
            remainingEarnLimit = dailyLimit.getRemainingEarnLimit(),
            remainingRandomEarnCount = dailyLimit.getRemainingRandomEarnCount(),
            todayEarned = dailyLimit.todayEarned,
            remainingEarnCount = dailyLimit.getRemainingEarnCount()
        )
    }
}

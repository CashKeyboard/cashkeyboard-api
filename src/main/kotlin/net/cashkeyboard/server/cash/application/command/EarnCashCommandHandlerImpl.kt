
package net.cashkeyboard.server.cash.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.cash.domain.*
import net.cashkeyboard.server.cash.domain.exception.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class EarnCashCommandHandlerImpl(
    private val cashAccountRepository: CashAccountRepository,
    private val cashTransactionRepository: CashTransactionRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val objectMapper: ObjectMapper
) : EarnCashCommandHandler {

    private val logger = LoggerFactory.getLogger(EarnCashCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: EarnCashCommand): EarnCashResult {
        logger.debug("Processing earn cash command: userId=${command.userId}, amount=${command.amount}, source=${command.source}")

        checkDuplicateSource(command.userId, command.sourceId)

        val cashAccount = getCashAccount(command.userId)

        val dailyLimit = getOrCreateDailyLimit(command.userId)
        validateEarnLimits(dailyLimit, command.amount)

        checkRateLimit(dailyLimit)

        val transaction = cashAccount.earn(command.amount)
        transaction.source = command.source
        transaction.sourceId = command.sourceId

        if (command.metadata != null) {
            transaction.metadata = objectMapper.writeValueAsString(command.metadata)
        }

        dailyLimit.recordEarn(command.amount)

        cashAccountRepository.save(cashAccount)
        val savedTransaction = cashTransactionRepository.save(transaction)
        dailyLimitRepository.save(dailyLimit)

        logger.info("Cash earned successfully: userId=${command.userId}, amount=${command.amount}, newBalance=${cashAccount.balance}")

        return EarnCashResult(
            transactionId = savedTransaction.id,
            earnedAmount = command.amount,
            newBalance = cashAccount.balance,
            dailyStatus = DailyStatus(
                todayEarned = dailyLimit.todayEarned,
                remainingLimit = dailyLimit.getRemainingEarnLimit(),
                todayEarnedCount = dailyLimit.todayEarnedCount,
                remainingRandomEarnCount = dailyLimit.getRemainingRandomEarnCount()
            ),
            timestamp = savedTransaction.createdAt
        )
    }

    private fun checkDuplicateSource(userId: UUID, sourceId: String) {
        val existingTransaction = cashTransactionRepository.findBySourceIdAndUserId(sourceId, userId)
        if (existingTransaction.isPresent) {
            throw DuplicateSourceIdException(sourceId, existingTransaction.get().id)
        }
    }

    private fun getCashAccount(userId: UUID): CashAccount {
        return cashAccountRepository.findByUserId(userId)
            .orElseThrow { CashAccountNotFoundException(userId) }
    }

    private fun getOrCreateDailyLimit(userId: UUID): DailyLimit {
        val today = LocalDate.now()
        return dailyLimitRepository.findByUserIdAndDate(userId, today)
            .orElseGet {
                DailyLimit(
                    userId = userId,
                    date = today
                )
            }
    }

    private fun validateEarnLimits(dailyLimit: DailyLimit, amount: Int) {
        if (!dailyLimit.canEarn(amount)) {
            throw DailyLimitExceededException(
                currentLimit = DailyLimit.MAX_DAILY_EARN,
                todayEarned = dailyLimit.todayEarned,
                requestedAmount = amount
            )
        }
    }

    private fun checkRateLimit(dailyLimit: DailyLimit) {
        if (dailyLimit.isEarnRateLimited()) {
            throw RateLimitExceededException("EARN", DailyLimit.EARN_RATE_LIMIT_SECONDS)
        }
    }
}

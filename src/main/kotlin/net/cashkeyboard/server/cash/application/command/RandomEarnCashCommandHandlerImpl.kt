package net.cashkeyboard.server.cash.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import net.cashkeyboard.server.cash.domain.*
import net.cashkeyboard.server.cash.domain.exception.*
import net.cashkeyboard.server.cash.domain.service.CashDomainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class RandomEarnCashCommandHandlerImpl(
    private val cashAccountRepository: CashAccountRepository,
    private val cashTransactionRepository: CashTransactionRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val cashDomainService: CashDomainService,
    private val objectMapper: ObjectMapper
) : RandomEarnCashCommandHandler {

    private val logger = LoggerFactory.getLogger(RandomEarnCashCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: RandomEarnCashCommand): RandomEarnCashResult {
        logger.debug("Processing random earn cash command: userId=${command.userId}, source=${command.source}")

        checkDuplicateSource(command.userId, command.sourceId)

        val cashAccount = getCashAccount(command.userId)

        val dailyLimit = getOrCreateDailyLimit(command.userId)
        validateRandomEarnLimits(dailyLimit)

        checkRateLimit(dailyLimit)

        val randomResult = cashDomainService.calculateRandomEarn(command.source)

        val transaction = cashAccount.earnRandom(
            amount = randomResult.amount,
            source = command.source,
            sourceId = command.sourceId
        )

        if (command.metadata != null) {
            val metadata = command.metadata.toMutableMap()
            metadata["tier"] = randomResult.tier
            metadata["winRate"] = randomResult.winRate
            transaction.metadata = objectMapper.writeValueAsString(metadata)
        }

        dailyLimit.recordRandomEarn()

        cashAccountRepository.save(cashAccount)
        val savedTransaction = if (randomResult.isWinner) {
            cashTransactionRepository.save(transaction)
        } else null
        dailyLimitRepository.save(dailyLimit)

        logger.info("Random cash earn processed: userId=${command.userId}, isWinner=${randomResult.isWinner}, amount=${randomResult.amount}")

        return RandomEarnCashResult(
            transactionId = savedTransaction?.id,
            isWinner = randomResult.isWinner,
            earnedAmount = randomResult.amount,
            newBalance = cashAccount.balance,
            randomResult = RandomEarnCashResult.RandomEarnResult(
                winRate = randomResult.winRate,
                tier = randomResult.tier,
                possibleAmounts = randomResult.possibleAmounts
            ),
            dailyStatus = RandomEarnDailyStatus(
                todayRandomEarnedCount = dailyLimit.todayRandomEarnedCount,
                remainingRandomEarnCount = dailyLimit.getRemainingRandomEarnCount()
            ),
            timestamp = savedTransaction?.createdAt ?: LocalDateTime.now()
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

    private fun validateRandomEarnLimits(dailyLimit: DailyLimit) {
        if (!dailyLimit.canRandomEarn()) {
            throw RandomEarnLimitExceededException(
                maxDailyCount = DailyLimit.MAX_RANDOM_EARN_COUNT,
                todayCount = dailyLimit.todayRandomEarnedCount
            )
        }
    }

    private fun checkRateLimit(dailyLimit: DailyLimit) {
        if (dailyLimit.isRandomEarnRateLimited()) {
            throw RateLimitExceededException("RANDOM_EARN", DailyLimit.RANDOM_EARN_RATE_LIMIT_SECONDS)
        }
    }
}

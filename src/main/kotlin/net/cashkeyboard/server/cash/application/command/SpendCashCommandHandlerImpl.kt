
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
class SpendCashCommandHandlerImpl(
    private val cashAccountRepository: CashAccountRepository,
    private val cashTransactionRepository: CashTransactionRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val objectMapper: ObjectMapper
) : SpendCashCommandHandler {

    private val logger = LoggerFactory.getLogger(SpendCashCommandHandlerImpl::class.java)

    @Transactional
    override fun handle(command: SpendCashCommand): SpendCashResult {
        logger.debug("Processing spend cash command: userId=${command.userId}, amount=${command.amount}, purpose=${command.purpose}")

        // 1. 캐시 계정 조회
        val cashAccount = getCashAccount(command.userId)

        // 2. 잔액 확인
        if (!cashAccount.canSpend(command.amount)) {
            throw InsufficientBalanceException(cashAccount.balance, command.amount)
        }

        // 3. Rate Limiting 체크 (선택사항)
        val dailyLimit = getOrCreateDailyLimit(command.userId)
        checkRateLimit(dailyLimit)

        // 4. 캐시 사용 처리
        val transaction = cashAccount.spend(
            amount = command.amount,
            purpose = command.purpose,
            targetId = command.targetId
        )

        if (command.metadata != null) {
            transaction.metadata = objectMapper.writeValueAsString(command.metadata)
        }

        // 5. 일일 한도 업데이트
        dailyLimit.recordSpend(command.amount)

        // 6. 저장
        cashAccountRepository.save(cashAccount)
        val savedTransaction = cashTransactionRepository.save(transaction)
        dailyLimitRepository.save(dailyLimit)

        logger.info("Cash spent successfully: userId=${command.userId}, amount=${command.amount}, newBalance=${cashAccount.balance}")

        return SpendCashResult(
            transactionId = savedTransaction.id,
            spentAmount = command.amount,
            newBalance = cashAccount.balance,
            timestamp = savedTransaction.createdAt
        )
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

    private fun checkRateLimit(dailyLimit: DailyLimit) {
        if (dailyLimit.isSpendRateLimited()) {
            throw RateLimitExceededException("SPEND", DailyLimit.SPEND_RATE_LIMIT_SECONDS)
        }
    }
}
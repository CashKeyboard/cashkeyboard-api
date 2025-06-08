package net.cashkeyboard.server.cash.application.command

import net.cashkeyboard.server.cash.domain.CashAccount
import net.cashkeyboard.server.cash.domain.CashAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateCashAccountCommandHandlerImpl(
    private val cashAccountRepository: CashAccountRepository
) : CreateCashAccountCommandHandler {

    @Transactional
    override fun handle(command: CreateCashAccountCommand): CreateCashAccountResult {
        val existingAccount = cashAccountRepository.findByUserId(command.userId)
        if (existingAccount.isPresent) {
            return CreateCashAccountResult(
                accountId = existingAccount.get().id,
                userId = command.userId
            )
        }

        val cashAccount = CashAccount(userId = command.userId)
        val savedAccount = cashAccountRepository.save(cashAccount)

        return CreateCashAccountResult(
            accountId = savedAccount.id,
            userId = savedAccount.userId
        )
    }
}

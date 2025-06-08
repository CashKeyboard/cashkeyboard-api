
package net.cashkeyboard.server.cash.application.service

import net.cashkeyboard.server.cash.application.command.CreateCashAccountCommand
import net.cashkeyboard.server.cash.application.command.CreateCashAccountCommandHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CashAccountService(
    private val createCashAccountCommandHandler: CreateCashAccountCommandHandler
) {

    @Transactional
    fun createCashAccountForUser(userId: UUID) {
        val command = CreateCashAccountCommand(userId = userId)
        createCashAccountCommandHandler.handle(command)
    }
}
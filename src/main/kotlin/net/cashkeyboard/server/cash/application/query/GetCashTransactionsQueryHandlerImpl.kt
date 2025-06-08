
// src/main/kotlin/net/cashkeyboard/server/cash/application/query/GetCashTransactionsQueryHandlerImpl.kt
package net.cashkeyboard.server.cash.application.query

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.cashkeyboard.server.cash.domain.CashTransactionRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetCashTransactionsQueryHandlerImpl(
    private val cashTransactionRepository: CashTransactionRepository,
    private val objectMapper: ObjectMapper
) : GetCashTransactionsQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetCashTransactionsQuery): Page<CashTransactionDto> {
        val transactions = cashTransactionRepository.findTransactionsWithFilters(
            userId = query.userId,
            type = query.type,
            source = query.source,
            startDate = query.startDate,
            endDate = query.endDate,
            pageable = query.pageable
        )

        return transactions.map { transaction ->
            val metadata: Map<String, Any>? = transaction.metadata?.let { metadataStr ->
                try {
                    objectMapper.readValue<Map<String, Any>>(metadataStr)
                } catch (e: Exception) {
                    null
                }
            }

            CashTransactionDto(
                id = transaction.id,
                type = transaction.type,
                amount = transaction.amount,
                source = transaction.source,
                sourceId = transaction.sourceId,
                purpose = transaction.purpose?.name,
                targetId = transaction.targetId,
                balanceAfter = transaction.balanceAfter,
                metadata = metadata,
                timestamp = transaction.createdAt
            )
        }
    }
}

package net.cashkeyboard.server.cash.application.query

import org.springframework.data.domain.Page

interface QueryHandler<T, R> {
    fun handle(query: T): R
}

interface GetCashAccountQueryHandler : QueryHandler<GetCashAccountQuery, CashAccountDto?>
interface GetCashTransactionsQueryHandler : QueryHandler<GetCashTransactionsQuery, Page<CashTransactionDto>>
interface GetDailyLimitsQueryHandler : QueryHandler<GetDailyLimitsQuery, DailyLimitsDto?>

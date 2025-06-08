package net.cashkeyboard.server.cash.application.command

interface CommandHandler<T, R> {
    fun handle(command: T): R
}

interface EarnCashCommandHandler : CommandHandler<EarnCashCommand, EarnCashResult>
interface RandomEarnCashCommandHandler : CommandHandler<RandomEarnCashCommand, RandomEarnCashResult>
interface SpendCashCommandHandler : CommandHandler<SpendCashCommand, SpendCashResult>
interface CreateCashAccountCommandHandler : CommandHandler<CreateCashAccountCommand, CreateCashAccountResult>
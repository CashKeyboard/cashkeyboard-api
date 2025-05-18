package net.cashkeyboard.server.auth.application.command

interface CommandHandler<T, R> {
    fun handle(command: T): R
}

interface LoginCommandHandler : CommandHandler<LoginCommand, LoginResult>
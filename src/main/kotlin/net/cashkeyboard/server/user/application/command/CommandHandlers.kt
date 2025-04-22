package net.cashkeyboard.server.user.application.command

import java.util.*

interface CommandHandler<T, R> {
    fun handle(command: T): R
}

interface CreateUserCommandHandler : CommandHandler<CreateUserCommand, UUID>
interface UpdateUserProfileCommandHandler : CommandHandler<UpdateUserProfileCommand, Unit>
//interface RegisterDeviceTokenCommandHandler : CommandHandler<RegisterDeviceTokenCommand, UUID>
//interface RemoveDeviceTokenCommandHandler : CommandHandler<RemoveDeviceTokenCommand, Unit>

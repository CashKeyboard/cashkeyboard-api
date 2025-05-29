package net.cashkeyboard.server.product.application.command

import java.util.*

interface CommandHandler<T, R> {
    fun handle(command: T): R
}

interface CreateProductCommandHandler : CommandHandler<CreateProductCommand, UUID>

interface UpdateProductCommandHandler : CommandHandler<UpdateProductCommand, Unit>

interface ActivateProductCommandHandler : CommandHandler<ActivateProductCommand, Unit>

interface DeactivateProductCommandHandler : CommandHandler<DeactivateProductCommand, Unit>
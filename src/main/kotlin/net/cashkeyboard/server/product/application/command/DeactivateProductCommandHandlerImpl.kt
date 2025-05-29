package net.cashkeyboard.server.product.application.command

import net.cashkeyboard.server.product.domain.ProductRepository
import net.cashkeyboard.server.product.domain.exception.ProductNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeactivateProductCommandHandlerImpl(
    private val productRepository: ProductRepository
) : DeactivateProductCommandHandler {

    @Transactional
    override fun handle(command: DeactivateProductCommand) {
        val product = productRepository.findById(command.productId)
            .orElseThrow { ProductNotFoundException(command.productId) }

        product.deactivateProduct()
        productRepository.save(product)
    }
}
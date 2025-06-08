package net.cashkeyboard.server.product.application.command

import net.cashkeyboard.server.product.domain.exception.ProductNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivateProductCommandHandlerImpl(
    private val productRepository: ProductRepository
) : ActivateProductCommandHandler {

    @Transactional
    override fun handle(command: ActivateProductCommand) {
        val product = productRepository.findById(command.productId)
            .orElseThrow { ProductNotFoundException(command.productId) }

        product.activateProduct()
        productRepository.save(product)
    }
}

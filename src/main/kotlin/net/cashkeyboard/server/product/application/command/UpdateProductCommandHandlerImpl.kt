package net.cashkeyboard.server.product.application.command

import net.cashkeyboard.server.product.domain.exception.InvalidProductDataException
import net.cashkeyboard.server.product.domain.exception.ProductNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateProductCommandHandlerImpl(
    private val productRepository: ProductRepository
) : UpdateProductCommandHandler {

    @Transactional
    override fun handle(command: UpdateProductCommand) {
        // Validation
        validateUpdateProductCommand(command)

        // Find product
        val product = productRepository.findById(command.productId)
            .orElseThrow { ProductNotFoundException(command.productId) }

        // Update product information (using domain method)
        product.updateProduct(
            name = command.name,
            description = command.description,
            price = command.price,
            imageUrl = command.imageUrl,
            category = command.category
        )

        productRepository.save(product)
    }

    private fun validateUpdateProductCommand(command: UpdateProductCommand) {
        when {
            command.name.isBlank() -> throw InvalidProductDataException("Product name is required")
            command.description.isBlank() -> throw InvalidProductDataException("Product description is required")
            command.price <= 0 -> throw InvalidProductDataException("Price must be greater than 0")
            command.imageUrl.isBlank() -> throw InvalidProductDataException("Image URL is required")
        }
    }
}
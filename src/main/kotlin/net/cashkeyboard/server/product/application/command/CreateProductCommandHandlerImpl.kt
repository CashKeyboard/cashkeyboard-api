package net.cashkeyboard.server.product.application.command

import net.cashkeyboard.server.product.domain.Product
import net.cashkeyboard.server.product.domain.ProductRepository
import net.cashkeyboard.server.product.domain.exception.DuplicateGoodsCodeException
import net.cashkeyboard.server.product.domain.exception.InvalidProductDataException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CreateProductCommandHandlerImpl(
    private val productRepository: ProductRepository
) : CreateProductCommandHandler {

    @Transactional
    override fun handle(command: CreateProductCommand): UUID {
        // Validation
        validateCreateProductCommand(command)

        // Check goodsCode duplication
        if (productRepository.findByGoodsCode(command.goodsCode).isPresent) {
            throw DuplicateGoodsCodeException(command.goodsCode)
        }

        // Create product
        val product = Product(
            name = command.name,
            description = command.description,
            price = command.price,
            imageUrl = command.imageUrl,
            goodsCode = command.goodsCode,
            stock = command.stock,
            category = command.category
        )

        val savedProduct = productRepository.save(product)
        return savedProduct.id
    }

    private fun validateCreateProductCommand(command: CreateProductCommand) {
        when {
            command.name.isBlank() -> throw InvalidProductDataException("Product name is required")
            command.description.isBlank() -> throw InvalidProductDataException("Product description is required")
            command.price <= 0 -> throw InvalidProductDataException("Price must be greater than 0")
            command.imageUrl.isBlank() -> throw InvalidProductDataException("Image URL is required")
            command.goodsCode.isBlank() -> throw InvalidProductDataException("Goods code is required")
            command.stock < 0 -> throw InvalidProductDataException("Stock must be greater than or equal to 0")
        }
    }
}
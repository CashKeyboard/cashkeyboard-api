package net.cashkeyboard.server.product.application.query

import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProductByIdQueryHandlerImpl(
    private val productRepository: ProductRepository
) : GetProductByIdQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetProductByIdQuery): ProductDto? {
        return productRepository.findById(query.productId)
            .map { ProductDto.from(it) }
            .orElse(null)
    }
}

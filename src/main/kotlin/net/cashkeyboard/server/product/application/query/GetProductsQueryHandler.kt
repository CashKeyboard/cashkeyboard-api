package net.cashkeyboard.server.product.application.query

import net.cashkeyboard.server.product.domain.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProductsQueryHandlerImpl(
    private val productRepository: ProductRepository
) : GetProductsQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetProductsQuery): org.springframework.data.domain.Page<ProductDto> {
        val products = if (query.activeOnly) {
            productRepository.findActiveProducts(query.pageable)
        } else {
            productRepository.findAll(query.pageable)
        }

        return products.map { ProductDto.from(it) }
    }
}

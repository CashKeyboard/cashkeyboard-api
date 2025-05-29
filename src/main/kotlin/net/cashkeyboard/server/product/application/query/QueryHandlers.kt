package net.cashkeyboard.server.product.application.query

import org.springframework.data.domain.Page
import java.time.LocalDateTime
import java.util.*

interface QueryHandler<T, R> {
    fun handle(query: T): R
}

interface GetProductByIdQueryHandler : QueryHandler<GetProductByIdQuery, ProductDto?>

interface GetProductsQueryHandler : QueryHandler<GetProductsQuery, Page<ProductDto>>

data class ProductDto(
    val id: UUID,
    val name: String,
    val description: String,
    val price: Int,
    val imageUrl: String,
    val goodsCode: String,
    val stock: Int,
    val category: String,
    val categoryDisplayName: String,
    val isActive: Boolean,
    val deactivatedAt: LocalDateTime?,
    val isPurchasable: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(product: net.cashkeyboard.server.product.domain.Product): ProductDto {
            return ProductDto(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                imageUrl = product.imageUrl,
                goodsCode = product.goodsCode,
                stock = product.stock,
                category = product.category.name,
                categoryDisplayName = product.category.displayName,
                isActive = product.isActive(),
                deactivatedAt = product.deactivatedAt,
                isPurchasable = product.isPurchasable(),
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

package net.cashkeyboard.server.product.application.query

import net.cashkeyboard.server.product.domain.ProductCategory
import org.springframework.data.domain.Pageable
import java.util.*


/**
 * Get single product by ID
 */
data class GetProductByIdQuery(
    val productId: UUID
)

/**
 * Get products with comprehensive filtering (unified query for all list operations)
 */
data class GetProductsQuery(
    val pageable: Pageable,
    val activeOnly: Boolean = true,
    val keyword: String? = null, // Search keyword
    val category: ProductCategory? = null, // Category filter
    val purchasableOnly: Boolean = false, // Active + in stock
    val lowStock: Boolean = false, // Low stock products
    val stockThreshold: Int = 10, // Threshold for low stock (used when lowStock=true)
    val minPrice: Int? = null, // Minimum price filter
    val maxPrice: Int? = null // Maximum price filter
)
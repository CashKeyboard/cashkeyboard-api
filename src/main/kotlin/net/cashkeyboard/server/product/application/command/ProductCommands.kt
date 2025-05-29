package net.cashkeyboard.server.product.application.command

import net.cashkeyboard.server.product.domain.ProductCategory
import java.util.*

/**
 * Create product command
 */
data class CreateProductCommand(
    val name: String,
    val description: String,
    val price: Int,
    val imageUrl: String,
    val goodsCode: String,
    val stock: Int,
    val category: ProductCategory
)

/**
 * Update product command
 */
data class UpdateProductCommand(
    val productId: UUID,
    val name: String,
    val description: String,
    val price: Int,
    val imageUrl: String,
    val category: ProductCategory
)

/**
 * Update stock command
 */
data class UpdateStockCommand(
    val productId: UUID,
    val quantity: Int,
    val operation: StockOperation
) {
    enum class StockOperation {
        INCREASE, // Stock increase
        DECREASE  // Stock decrease (purchase, etc.)
    }
}

/**
 * Activate product command
 */
data class ActivateProductCommand(
    val productId: UUID
)

/**
 * Deactivate product command
 */
data class DeactivateProductCommand(
    val productId: UUID
)
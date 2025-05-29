package net.cashkeyboard.server.product.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import net.cashkeyboard.server.product.application.query.ProductDto
import java.time.LocalDateTime
import java.util.*

data class ProductResponse(
    @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Product name", example = "Starbucks Americano")
    val name: String,

    @Schema(description = "Product description", example = "Rich espresso taste")
    val description: String,

    @Schema(description = "Product price in cash", example = "4500")
    val price: Int,

    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    val imageUrl: String,

    @Schema(description = "Gifticon goods code", example = "SB001")
    val goodsCode: String,

    @Schema(description = "Current stock quantity", example = "100")
    val stock: Int,

    @Schema(description = "Product category code", example = "COFFEE")
    val category: String,

    @Schema(description = "Product category display name", example = "Coffee")
    val categoryDisplayName: String,

    @Schema(description = "Whether product is active", example = "true")
    val isActive: Boolean,

    @Schema(description = "Deactivated timestamp (null if active)", example = "null")
    val deactivatedAt: LocalDateTime?,

    @Schema(description = "Whether product is purchasable", example = "true")
    val isPurchasable: Boolean,

    @Schema(description = "Creation timestamp", example = "2024-01-01T00:00:00")
    val createdAt: LocalDateTime,

    @Schema(description = "Last update timestamp", example = "2024-01-01T00:00:00")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(dto: ProductDto): ProductResponse {
            return ProductResponse(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                price = dto.price,
                imageUrl = dto.imageUrl,
                goodsCode = dto.goodsCode,
                stock = dto.stock,
                category = dto.category,
                categoryDisplayName = dto.categoryDisplayName,
                isActive = dto.isActive,
                deactivatedAt = dto.deactivatedAt,
                isPurchasable = dto.isPurchasable,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}

data class ProductSummaryResponse(
    @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,

    @Schema(description = "Product name", example = "Starbucks Americano")
    val name: String,

    @Schema(description = "Product price in cash", example = "4500")
    val price: Int,

    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    val imageUrl: String,

    @Schema(description = "Product category code", example = "COFFEE")
    val category: String,

    @Schema(description = "Product category display name", example = "Coffee")
    val categoryDisplayName: String,

    @Schema(description = "Whether product is active", example = "true")
    val isActive: Boolean,

    @Schema(description = "Whether product is purchasable", example = "true")
    val isPurchasable: Boolean
) {
    companion object {
        fun from(dto: ProductDto): ProductSummaryResponse {
            return ProductSummaryResponse(
                id = dto.id,
                name = dto.name,
                price = dto.price,
                imageUrl = dto.imageUrl,
                category = dto.category,
                categoryDisplayName = dto.categoryDisplayName,
                isActive = dto.isActive,
                isPurchasable = dto.isPurchasable
            )
        }
    }
}

data class CreateProductResponse(
    @Schema(description = "Created product ID", example = "123e4567-e89b-12d3-a456-426614174000")
    val productId: UUID,

    @Schema(description = "Success message", example = "Product created successfully")
    val message: String = "Product created successfully"
)

data class StockUpdateResponse(
    @Schema(description = "Updated stock quantity", example = "110")
    val currentStock: Int,

    @Schema(description = "Success message", example = "Stock updated successfully")
    val message: String = "Stock updated successfully"
)
package net.cashkeyboard.server.product.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import net.cashkeyboard.server.common.validation.ValidEnum
import net.cashkeyboard.server.product.domain.ProductCategory

data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Starbucks Americano")
    val name: String,

    @field:NotBlank(message = "Product description is required")
    @Schema(description = "Product description", example = "Rich espresso taste")
    val description: String,

    @field:NotNull(message = "Price is required")
    @field:Min(value = 1, message = "Price must be greater than 0")
    @Schema(description = "Product price in cash", example = "4500")
    val price: Int,

    @field:NotBlank(message = "Image URL is required")
    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    val imageUrl: String,

    @field:NotBlank(message = "Goods code is required")
    @Schema(description = "Gifticon goods code", example = "SB001")
    val goodsCode: String,

    @field:NotNull(message = "Stock is required")
    @field:Min(value = 0, message = "Stock must be greater than or equal to 0")
    @Schema(description = "Initial stock quantity", example = "100")
    val stock: Int,

    @field:ValidEnum(enumClass = ProductCategory::class)
    @Schema(
        description = "Product category",
        example = "COFFEE",
        allowableValues = ["COFFEE", "FOOD", "GIFT_CARD", "FASHION", "DIGITAL", "CONVENIENCE", "ENTERTAINMENT", "BEAUTY", "LIFESTYLE", "ETC"]
    )
    val category: String
)

data class UpdateProductRequest(
    @field:NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Starbucks Americano")
    val name: String,

    @field:NotBlank(message = "Product description is required")
    @Schema(description = "Product description", example = "Rich espresso taste")
    val description: String,

    @field:NotNull(message = "Price is required")
    @field:Min(value = 1, message = "Price must be greater than 0")
    @Schema(description = "Product price in cash", example = "4500")
    val price: Int,

    @field:NotBlank(message = "Image URL is required")
    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    val imageUrl: String,

    @field:ValidEnum(enumClass = ProductCategory::class)
    @Schema(
        description = "Product category",
        example = "COFFEE",
        allowableValues = ["COFFEE", "FOOD", "GIFT_CARD", "FASHION", "DIGITAL", "CONVENIENCE", "ENTERTAINMENT", "BEAUTY", "LIFESTYLE", "ETC"]
    )
    val category: String
)

data class UpdateStockRequest(
    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be greater than 0")
    @Schema(description = "Stock quantity to increase or decrease", example = "10")
    val quantity: Int,

    @field:NotBlank(message = "Operation is required")
    @Schema(
        description = "Stock operation type",
        example = "INCREASE",
        allowableValues = ["INCREASE", "DECREASE"]
    )
    val operation: String
)
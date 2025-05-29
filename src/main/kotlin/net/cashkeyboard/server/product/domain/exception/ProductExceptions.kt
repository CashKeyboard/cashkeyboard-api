// src/main/kotlin/net/cashkeyboard/server/product/domain/exception/ProductExceptions.kt
package net.cashkeyboard.server.product.domain.exception

import net.cashkeyboard.server.common.errors.ErrorCode
import net.cashkeyboard.server.common.errors.RestApiException
import org.springframework.http.HttpStatus
import java.util.*

enum class ProductErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Product is not active"),
    PRODUCT_NOT_PURCHASABLE(HttpStatus.BAD_REQUEST, "Product is not purchasable"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Insufficient stock"),
    INVALID_PRODUCT_DATA(HttpStatus.BAD_REQUEST, "Invalid product data"),
    DUPLICATE_GOODS_CODE(HttpStatus.CONFLICT, "Product with this goods code already exists"),
    INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "Invalid stock quantity"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "Invalid price");

    override fun getCode(): String {
        return name
    }
}

class ProductNotFoundException(productId: UUID) :
    RestApiException(ProductErrorCode.PRODUCT_NOT_FOUND)

class ProductNotActiveException(productId: UUID) :
    RestApiException(ProductErrorCode.PRODUCT_NOT_ACTIVE)

class ProductNotPurchasableException(productId: UUID) :
    RestApiException(ProductErrorCode.PRODUCT_NOT_PURCHASABLE)

class InsufficientStockException(available: Int, requested: Int) :
    RestApiException(ProductErrorCode.INSUFFICIENT_STOCK)

class InvalidProductDataException(message: String) :
    RestApiException(ProductErrorCode.INVALID_PRODUCT_DATA)

class DuplicateGoodsCodeException(goodsCode: String) :
    RestApiException(ProductErrorCode.DUPLICATE_GOODS_CODE)

class InvalidStockQuantityException(quantity: Int) :
    RestApiException(ProductErrorCode.INVALID_STOCK_QUANTITY)

class InvalidPriceException(price: Int) :
    RestApiException(ProductErrorCode.INVALID_PRICE)
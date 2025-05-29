package net.cashkeyboard.server.product.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false)
    var price: Int, // 캐시 가격

    @Column(nullable = false)
    var imageUrl: String, // S3 이미지 URL

    @Column(nullable = false)
    var goodsCode: String, // 기프티콘 상품 ID

    @Column(nullable = false)
    var stock: Int = 0, // 재고

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: ProductCategory,

    @Column(nullable = true)
    var deactivatedAt: LocalDateTime? = null

) : BaseTimeEntity() {

    fun isActive(): Boolean = deactivatedAt == null

    fun isPurchasable(): Boolean = isActive() && stock > 0

    fun decreaseStock(quantity: Int) {
        require(quantity > 0) { "수량은 0보다 커야 합니다" }
        require(stock >= quantity) { "재고가 부족합니다. 현재 재고: $stock, 요청 수량: $quantity" }

        stock -= quantity

        // 재고가 0이 되면 자동으로 비활성화
        if (stock == 0 && isActive()) {
            deactivateProduct()
        }
    }

    fun increaseStock(quantity: Int) {
        require(quantity > 0) { "수량은 0보다 커야 합니다" }
        stock += quantity

        // 재고가 생기면 비활성화가 재고 부족으로 인한 것이었다면 다시 활성화할 수 있음
        // (수동 비활성화와 구분하기 위해 별도 처리 필요할 수 있음)
    }

    fun deactivateProduct() {
        if (isActive()) {
            deactivatedAt = LocalDateTime.now()
        }
    }

    fun activateProduct() {
        deactivatedAt = null
    }

    fun updateProduct(
        name: String,
        description: String,
        price: Int,
        imageUrl: String,
        category: ProductCategory
    ) {
        require(price > 0) { "가격은 0보다 커야 합니다" }
        require(name.isNotBlank()) { "상품명은 필수입니다" }
        require(description.isNotBlank()) { "상품 설명은 필수입니다" }
        require(imageUrl.isNotBlank()) { "이미지 URL은 필수입니다" }

        this.name = name
        this.description = description
        this.price = price
        this.imageUrl = imageUrl
        this.category = category
    }

    fun syncWithGifticon(
        name: String,
        description: String,
        price: Int,
        stock: Int
    ) {
        this.name = name
        this.description = description
        this.price = price

        if (stock > this.stock) {
            this.stock = stock
        }
    }
}
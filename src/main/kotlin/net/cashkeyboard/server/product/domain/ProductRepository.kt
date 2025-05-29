package net.cashkeyboard.server.product.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface ProductRepository : JpaRepository<Product, UUID> {

    /**
     * Find active products (paginated)
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL")
    fun findActiveProducts(pageable: Pageable): Page<Product>

    /**
     * Find active products by category (paginated)
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL AND p.category = :category")
    fun findActiveProductsByCategory(
        @Param("category") category: ProductCategory,
        pageable: Pageable
    ): Page<Product>

    /**
     * Find purchasable products (active + in stock)
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL AND p.stock > 0")
    fun findPurchasableProducts(pageable: Pageable): Page<Product>

    /**
     * Search active products by name
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL AND p.name LIKE %:keyword%")
    fun searchActiveProductsByName(@Param("keyword") keyword: String, pageable: Pageable): Page<Product>

    /**
     * Find products with complex filtering
     */
    @Query("""
        SELECT p FROM Product p WHERE 
        (:activeOnly = false OR p.deactivatedAt IS NULL) AND
        (:keyword IS NULL OR p.name LIKE %:keyword%) AND
        (:category IS NULL OR p.category = :category) AND
        (:purchasableOnly = false OR (p.deactivatedAt IS NULL AND p.stock > 0)) AND
        (:minPrice IS NULL OR p.price >= :minPrice) AND
        (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    fun findProductsWithFilters(
        @Param("activeOnly") activeOnly: Boolean,
        @Param("keyword") keyword: String?,
        @Param("category") category: ProductCategory?,
        @Param("purchasableOnly") purchasableOnly: Boolean,
        @Param("minPrice") minPrice: Int?,
        @Param("maxPrice") maxPrice: Int?,
        pageable: Pageable
    ): Page<Product>

    /**
     * Find product by goods code
     */
    fun findByGoodsCode(goodsCode: String): Optional<Product>

    /**
     * Find low stock products (below threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL AND p.stock <= :threshold")
    fun findLowStockProducts(@Param("threshold") threshold: Int): List<Product>

    /**
     * Find active products with zero stock (for auto deactivation)
     */
    @Query("SELECT p FROM Product p WHERE p.deactivatedAt IS NULL AND p.stock = 0")
    fun findActiveProductsWithZeroStock(): List<Product>
}
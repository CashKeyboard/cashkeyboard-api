package net.cashkeyboard.server.product.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import net.cashkeyboard.server.product.api.dto.ProductSummaryResponse
import net.cashkeyboard.server.product.application.query.GetProductsQuery
import net.cashkeyboard.server.product.application.query.GetProductsQueryHandler
import net.cashkeyboard.server.product.domain.ProductCategory
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product API", description = "Product API for users")
@SecurityRequirement(name = "Bearer Authentication")
class ProductController(
    private val getProductsQueryHandler: GetProductsQueryHandler
) {
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping
    @Operation(
        summary = "Get products with filtering",
        description = """
            Get product list with comprehensive filtering options.
            
            **Filtering Options:**
            - `keyword`: Search products by name
            - `category`: Filter by product category
            - `purchasableOnly`: Show only purchasable products (active + in stock)
            - `lowStock`: Show only low stock products 
            - `stockThreshold`: Threshold for low stock filter (default: 10)
            - `minPrice`, `maxPrice`: Price range filter
            - `activeOnly`: Show only active products (default: true)
            
            **Sorting Options:**
            - `createdAt,desc` - Latest first (default)
            - `createdAt,asc` - Oldest first
            - `name,asc` - Name A-Z
            - `name,desc` - Name Z-A
            - `price,asc` - Price low to high
            - `price,desc` - Price high to low
            - `stock,asc` - Stock low to high
            - `stock,desc` - Stock high to low
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Products retrieved successfully",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(responseCode = "401", description = "Authentication required")
        ]
    )
    fun getProducts(
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable,

        @Parameter(description = "Search keyword for product name")
        @RequestParam(required = false) keyword: String?,

        @Parameter(
            description = "Product category filter",
            schema = Schema(
                allowableValues = ["COFFEE", "FOOD", "GIFT_CARD", "FASHION", "DIGITAL", "CONVENIENCE", "ENTERTAINMENT", "BEAUTY", "LIFESTYLE", "ETC"]
            )
        )
        @RequestParam(required = false) category: String?,

        @Parameter(description = "Show only purchasable products (active + in stock)")
        @RequestParam(defaultValue = "false") purchasableOnly: Boolean,

        @Parameter(description = "Show only low stock products")
        @RequestParam(defaultValue = "false") lowStock: Boolean,

        @Parameter(description = "Stock threshold for low stock filter")
        @RequestParam(defaultValue = "10") stockThreshold: Int,

        @Parameter(description = "Minimum price filter")
        @RequestParam(required = false) minPrice: Int?,

        @Parameter(description = "Maximum price filter")
        @RequestParam(required = false) maxPrice: Int?,

        @Parameter(description = "Show only active products")
        @RequestParam(defaultValue = "true") activeOnly: Boolean
    ): ResponseEntity<Page<ProductSummaryResponse>> {

        logger.debug("=== getProducts called ===")
        logger.debug("Filters - keyword: $keyword, category: $category, purchasableOnly: $purchasableOnly")
        logger.debug("Price range - min: $minPrice, max: $maxPrice")
        logger.debug("Stock filters - lowStock: $lowStock, threshold: $stockThreshold")
        logger.debug("Pageable - page: ${pageable.pageNumber}, size: ${pageable.pageSize}, sort: ${pageable.sort}")

        // Validate category if provided
        val categoryEnum = category?.let { categoryStr ->
            try {
                ProductCategory.valueOf(categoryStr.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid category provided: $categoryStr")
                return ResponseEntity.badRequest().build()
            }
        }

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            logger.warn("Invalid price range: min($minPrice) > max($maxPrice)")
            return ResponseEntity.badRequest().build()
        }

        // Validate stock threshold
        if (stockThreshold < 0) {
            logger.warn("Invalid stock threshold: $stockThreshold")
            return ResponseEntity.badRequest().build()
        }

        val query = GetProductsQuery(
            pageable = pageable,
            activeOnly = activeOnly,
            keyword = keyword?.takeIf { it.isNotBlank() },
            category = categoryEnum,
            purchasableOnly = purchasableOnly,
            lowStock = lowStock,
            stockThreshold = stockThreshold,
            minPrice = minPrice,
            maxPrice = maxPrice
        )

        val products = getProductsQueryHandler.handle(query)
        val response = products.map { ProductSummaryResponse.from(it) }

        logger.debug("Retrieved ${response.content.size} products out of ${response.totalElements} total")

        return ResponseEntity.ok(response)
    }
}
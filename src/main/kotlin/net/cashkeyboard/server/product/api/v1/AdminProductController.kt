package net.cashkeyboard.server.product.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.cashkeyboard.server.product.api.dto.*
import net.cashkeyboard.server.product.application.command.*
import net.cashkeyboard.server.product.application.query.GetProductByIdQuery
import net.cashkeyboard.server.product.application.query.GetProductByIdQueryHandler
import net.cashkeyboard.server.product.domain.ProductCategory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "Admin Product API", description = "Product management API for administrators")
@SecurityRequirement(name = "Admin Key")
class AdminProductController(
    private val createProductCommandHandler: CreateProductCommandHandler,
    private val updateProductCommandHandler: UpdateProductCommandHandler,
    private val activateProductCommandHandler: ActivateProductCommandHandler,
    private val deactivateProductCommandHandler: DeactivateProductCommandHandler,
    private val getProductByIdQueryHandler: GetProductByIdQueryHandler
) {

    @PostMapping
    @Operation(
        summary = "Create new product",
        description = "Create a new product. Requires admin authentication.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Product created successfully",
                content = [Content(schema = Schema(implementation = CreateProductResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid product data"),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "409", description = "Product with goods code already exists")
        ]
    )
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest
    ): ResponseEntity<CreateProductResponse> {
        val command = CreateProductCommand(
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            goodsCode = request.goodsCode,
            stock = request.stock,
            category = ProductCategory.valueOf(request.category)
        )

        val productId = createProductCommandHandler.handle(command)

        val response = CreateProductResponse(productId = productId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieve product details by ID. Requires admin authentication.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Product retrieved successfully",
                content = [Content(schema = Schema(implementation = ProductResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun getProduct(
        @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable productId: UUID
    ): ResponseEntity<ProductResponse> {
        val query = GetProductByIdQuery(productId)
        val productDto = getProductByIdQueryHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        val response = ProductResponse.from(productDto)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{productId}")
    @Operation(
        summary = "Update product",
        description = "Update product information. Requires admin authentication.",
        responses = [
            ApiResponse(responseCode = "200", description = "Product updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid product data"),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun updateProduct(
        @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable productId: UUID,
        @Valid @RequestBody request: UpdateProductRequest
    ): ResponseEntity<Map<String, String>> {
        val command = UpdateProductCommand(
            productId = productId,
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            category = ProductCategory.valueOf(request.category)
        )

        updateProductCommandHandler.handle(command)

        return ResponseEntity.ok(mapOf("message" to "Product updated successfully"))
    }

    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Delete product (deactivate)",
        description = "Deactivate product (soft delete). Requires admin authentication.",
        responses = [
            ApiResponse(responseCode = "200", description = "Product deactivated successfully"),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun deleteProduct(
        @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable productId: UUID
    ): ResponseEntity<Map<String, String>> {
        val command = DeactivateProductCommand(productId)
        deactivateProductCommandHandler.handle(command)

        return ResponseEntity.ok(mapOf("message" to "Product deactivated successfully"))
    }

    @PostMapping("/{productId}/activate")
    @Operation(
        summary = "Activate product",
        description = "Activate a deactivated product. Requires admin authentication.",
        responses = [
            ApiResponse(responseCode = "200", description = "Product activated successfully"),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun activateProduct(
        @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable productId: UUID
    ): ResponseEntity<Map<String, String>> {
        val command = ActivateProductCommand(productId)
        activateProductCommandHandler.handle(command)

        return ResponseEntity.ok(mapOf("message" to "Product activated successfully"))
    }

    @PostMapping("/{productId}/deactivate")
    @Operation(
        summary = "Deactivate product",
        description = "Deactivate an active product. Requires admin authentication.",
        responses = [
            ApiResponse(responseCode = "200", description = "Product deactivated successfully"),
            ApiResponse(responseCode = "401", description = "Admin authentication required"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    fun deactivateProduct(
        @Parameter(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable productId: UUID
    ): ResponseEntity<Map<String, String>> {
        val command = DeactivateProductCommand(productId)
        deactivateProductCommandHandler.handle(command)

        return ResponseEntity.ok(mapOf("message" to "Product deactivated successfully"))
    }
}
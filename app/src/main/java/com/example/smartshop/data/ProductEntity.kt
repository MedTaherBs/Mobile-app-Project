package com.example.smartshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for Product
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val imagePath: String? = null
) {
    /**
     * Convert to domain model
     */
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            quantity = quantity,
            price = price,
            imagePath = imagePath
        )
    }
    
    companion object {
        /**
         * Create from domain model
         */
        fun fromProduct(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                quantity = product.quantity,
                price = product.price,
                imagePath = product.imagePath
            )
        }
    }
}

package com.example.projetmobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cart item data model
 */
data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImagePath: String? = null,
    val quantity: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun getTotalPrice(): Double = productPrice * quantity
}

/**
 * Room Entity for CartItem
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImagePath: String?,
    val quantity: Int,
    val addedAt: Long
) {
    fun toCartItem(): CartItem {
        return CartItem(
            id = id,
            userId = userId,
            productId = productId,
            productName = productName,
            productPrice = productPrice,
            productImagePath = productImagePath,
            quantity = quantity,
            addedAt = addedAt
        )
    }
    
    companion object {
        fun fromCartItem(item: CartItem): CartItemEntity {
            return CartItemEntity(
                id = item.id,
                userId = item.userId,
                productId = item.productId,
                productName = item.productName,
                productPrice = item.productPrice,
                productImagePath = item.productImagePath,
                quantity = item.quantity,
                addedAt = item.addedAt
            )
        }
    }
}

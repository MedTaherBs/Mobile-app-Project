package com.example.smartshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Order data model
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "Completed"
)

/**
 * Order item (product in an order)
 */
data class OrderItem(
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val productImagePath: String? = null
)

/**
 * Room Entity for Order
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val itemsJson: String, // JSON string of items
    val totalAmount: Double,
    val orderDate: Long,
    val status: String
)

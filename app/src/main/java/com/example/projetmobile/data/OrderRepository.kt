package com.example.projetmobile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for Order operations
 */
class OrderRepository(
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val cartDao: CartDao
) {
    
    fun getOrders(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersByUser(userId).map { entities ->
            entities.map { entity ->
                Order(
                    id = entity.id,
                    userId = entity.userId,
                    items = parseOrderItems(entity.itemsJson),
                    totalAmount = entity.totalAmount,
                    orderDate = entity.orderDate,
                    status = entity.status
                )
            }
        }
    }
    
    suspend fun placeOrder(userId: String, cartItems: List<CartItem>): Result<String> {
        if (cartItems.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }
        
        return try {
            // Validate stock for all items
            for (cartItem in cartItems) {
                val product = productDao.getProductById(cartItem.productId)?.toProduct()
                if (product == null) {
                    return Result.failure(Exception("Product ${cartItem.productName} not found"))
                }
                if (product.quantity < cartItem.quantity) {
                    return Result.failure(Exception("Insufficient stock for ${cartItem.productName}. Available: ${product.quantity}"))
                }
            }
            
            // Deduct quantities from products
            for (cartItem in cartItems) {
                val product = productDao.getProductById(cartItem.productId)?.toProduct()
                if (product != null) {
                    val updatedProduct = product.copy(quantity = product.quantity - cartItem.quantity)
                    productDao.updateProduct(ProductEntity.fromProduct(updatedProduct))
                }
            }
            
            // Create order
            val orderId = java.util.UUID.randomUUID().toString()
            val orderItems = cartItems.map {
                OrderItem(
                    productId = it.productId,
                    productName = it.productName,
                    productPrice = it.productPrice,
                    quantity = it.quantity,
                    productImagePath = it.productImagePath
                )
            }
            
            val totalAmount = cartItems.sumOf { it.getTotalPrice() }
            
            val orderEntity = OrderEntity(
                id = orderId,
                userId = userId,
                itemsJson = orderItemsToJson(orderItems),
                totalAmount = totalAmount,
                orderDate = System.currentTimeMillis(),
                status = "Completed"
            )
            
            orderDao.insertOrder(orderEntity)
            
            // Clear cart
            cartDao.clearCart(userId)
            
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun orderItemsToJson(items: List<OrderItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("productId", item.productId)
                put("productName", item.productName)
                put("productPrice", item.productPrice)
                put("quantity", item.quantity)
                put("productImagePath", item.productImagePath ?: "")
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
    
    private fun parseOrderItems(json: String): List<OrderItem> {
        val items = mutableListOf<OrderItem>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                items.add(
                    OrderItem(
                        productId = jsonObject.getString("productId"),
                        productName = jsonObject.getString("productName"),
                        productPrice = jsonObject.getDouble("productPrice"),
                        quantity = jsonObject.getInt("quantity"),
                        productImagePath = jsonObject.optString("productImagePath").takeIf { it.isNotEmpty() }
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }
    
    suspend fun getOrderCount(userId: String): Int {
        return orderDao.getOrderCount(userId)
    }
}

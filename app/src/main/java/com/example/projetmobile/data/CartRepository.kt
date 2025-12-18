package com.example.projetmobile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Cart operations
 */
class CartRepository(
    private val cartDao: CartDao,
    private val productDao: ProductDao
) {
    
    fun getCartItems(userId: String): Flow<List<CartItem>> {
        return cartDao.getCartItemsByUser(userId).map { entities ->
            entities.map { it.toCartItem() }
        }
    }
    
    suspend fun addToCart(userId: String, product: Product, quantity: Int): Result<Unit> {
        // Check product stock
        val availableStock = product.quantity
        if (quantity > availableStock) {
            return Result.failure(Exception("Insufficient stock. Available: $availableStock"))
        }
        
        if (quantity <= 0) {
            return Result.failure(Exception("Quantity must be positive"))
        }
        
        // Check if product already in cart
        val existingItem = cartDao.getCartItemByProduct(userId, product.id)
        
        return try {
            if (existingItem != null) {
                // Update quantity
                val newQuantity = existingItem.quantity + quantity
                if (newQuantity > availableStock) {
                    return Result.failure(Exception("Total quantity exceeds stock. Available: $availableStock"))
                }
                val updated = existingItem.copy(quantity = newQuantity)
                cartDao.updateCartItem(updated)
            } else {
                // Add new item
                val cartItem = CartItem(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId,
                    productId = product.id,
                    productName = product.name,
                    productPrice = product.price,
                    productImagePath = product.imagePath,
                    quantity = quantity
                )
                cartDao.insertCartItem(CartItemEntity.fromCartItem(cartItem))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCartItemQuantity(itemId: String, newQuantity: Int, productId: String): Result<Unit> {
        if (newQuantity <= 0) {
            return Result.failure(Exception("Quantity must be positive"))
        }
        
        // Check stock
        val product = productDao.getProductById(productId)?.toProduct()
        if (product == null) {
            return Result.failure(Exception("Product not found"))
        }
        
        if (newQuantity > product.quantity) {
            return Result.failure(Exception("Insufficient stock. Available: ${product.quantity}"))
        }
        
        return try {
            val item = cartDao.getCartItemById(itemId)
            if (item != null) {
                val updated = item.copy(quantity = newQuantity)
                cartDao.updateCartItem(updated)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cart item not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeFromCart(itemId: String): Result<Unit> {
        return try {
            val item = cartDao.getCartItemById(itemId)
            if (item != null) {
                cartDao.deleteCartItem(item)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cart item not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCart(userId: String) {
        cartDao.clearCart(userId)
    }
    
    suspend fun getCartItemCount(userId: String): Int {
        return cartDao.getCartItemCount(userId)
    }
    
    suspend fun getCartTotal(userId: String): Double {
        return cartDao.getCartTotal(userId) ?: 0.0
    }
}

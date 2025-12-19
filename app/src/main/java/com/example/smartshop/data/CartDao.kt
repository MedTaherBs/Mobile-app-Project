package com.example.smartshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Cart operations
 */
@Dao
interface CartDao {
    
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getCartItemsByUser(userId: String): Flow<List<CartItemEntity>>
    
    @Query("SELECT * FROM cart_items WHERE id = :itemId")
    suspend fun getCartItemById(itemId: String): CartItemEntity?
    
    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun getCartItemByProduct(userId: String, productId: String): CartItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)
    
    @Update
    suspend fun updateCartItem(item: CartItemEntity)
    
    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)
    
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
    
    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    suspend fun getCartItemCount(userId: String): Int
    
    @Query("SELECT SUM(productPrice * quantity) FROM cart_items WHERE userId = :userId")
    suspend fun getCartTotal(userId: String): Double?
}

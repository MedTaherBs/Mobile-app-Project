package com.example.projetmobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Order operations
 */
@Dao
interface OrderDao {
    
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrdersByUser(userId: String): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
    
    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId")
    suspend fun getOrderCount(userId: String): Int
}

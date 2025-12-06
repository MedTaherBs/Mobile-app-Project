package com.example.projetmobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) for Product CRUD operations
 */
@Dao
interface ProductDao {
    
    /**
     * Get all products as Flow for reactive updates
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    /**
     * Get a product by ID
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?
    
    /**
     * Insert a new product
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    /**
     * Update an existing product
     */
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    /**
     * Delete a product
     */
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    /**
     * Delete product by ID
     */
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)
    
    /**
     * Delete all products
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    /**
     * Get product count
     */
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

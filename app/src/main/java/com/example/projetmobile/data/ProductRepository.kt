package com.example.projetmobile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Product data operations
 * Handles data layer logic with Room database and Firestore cloud sync
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val firestoreService: FirestoreService
) {
    
    /**
     * Get all products from local database as Flow
     */
    val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toProduct() }
    }
    
    /**
     * Get product by ID from local database
     */
    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toProduct()
    }
    
    /**
     * Insert a product (local + cloud)
     */
    suspend fun insertProduct(product: Product, userId: String) {
        // Save to local database
        productDao.insertProduct(ProductEntity.fromProduct(product))
        
        // Sync to Firestore
        firestoreService.addProduct(userId, product)
    }
    
    /**
     * Update a product (local + cloud)
     */
    suspend fun updateProduct(product: Product, userId: String) {
        // Update local database
        productDao.updateProduct(ProductEntity.fromProduct(product))
        
        // Sync to Firestore
        firestoreService.updateProduct(userId, product)
    }
    
    /**
     * Delete a product (local + cloud)
     */
    suspend fun deleteProduct(product: Product) {
        // Delete from local database
        productDao.deleteProductById(product.id)
        
        // Delete from Firestore
        firestoreService.deleteProduct(product.id)
    }
    
    /**
     * Sync cloud products to local database
     */
    suspend fun syncFromCloud(userId: String) {
        // This will be handled by real-time listener in ViewModel
    }
    
    /**
     * Sync local products to cloud
     */
    suspend fun syncToCloud(userId: String) {
        val localProducts = productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }
        
        // Get first emission
        localProducts.collect { products ->
            firestoreService.syncToCloud(userId, products)
            return@collect // Exit after first emission
        }
    }
    
    /**
     * Get real-time cloud products
     */
    fun getCloudProducts(userId: String): Flow<List<Product>> {
        return firestoreService.getAllProducts(userId)
    }
    
    /**
     * Delete all products
     */
    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }
    
    /**
     * Get product count
     */
    suspend fun getProductCount(): Int {
        return productDao.getProductCount()
    }
}

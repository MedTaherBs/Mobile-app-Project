package com.example.smartshop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.data.AppDatabase
import com.example.smartshop.data.FirestoreService
import com.example.smartshop.data.Product
import com.example.smartshop.data.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing products with Room persistence and Firestore cloud sync
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ProductRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    init {
        val database = AppDatabase.getDatabase(application)
        val firestoreService = FirestoreService()
        repository = ProductRepository(database.productDao(), firestoreService)
    }
    
    val products: StateFlow<List<Product>> = MutableStateFlow(emptyList())
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()
    
    init {
        // Start listening to cloud updates in real-time
        startCloudSync()
        
        // Also listen to local database
        viewModelScope.launch {
            repository.allProducts.collect { productList ->
                (products as MutableStateFlow).value = productList
            }
        }
    }
    
    /**
     * Start real-time cloud synchronization
     */
    private fun startCloudSync() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                repository.getCloudProducts(userId).collect { cloudProducts ->
                    _syncStatus.value = "Synced with cloud"
                    
                    // Merge cloud products to local database
                    cloudProducts.forEach { cloudProduct ->
                        val localProduct = repository.getProductById(cloudProduct.id)
                        if (localProduct == null) {
                            // New product from cloud - add to local
                            repository.insertProduct(cloudProduct, userId)
                        }
                    }
                }
            } catch (e: Exception) {
                _syncStatus.value = "Sync error: ${e.message}"
            }
        }
    }
    
    /**
     * Add a new product
     */
    fun addProduct(name: String, quantity: Int, price: Double, imagePath: String? = null) {
        val product = Product(
            id = UUID.randomUUID().toString(),
            name = name,
            quantity = quantity,
            price = price,
            imagePath = imagePath
        )
        
        val validationError = product.getValidationError()
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.insertProduct(product, userId)
                _errorMessage.value = null
                _syncStatus.value = "Product added and synced"
            } catch (e: Exception) {
                _errorMessage.value = "Error adding product: ${e.message}"
            }
        }
    }
    
    /**
     * Update an existing product
     */
    fun updateProduct(id: String, name: String, quantity: Int, price: Double, imagePath: String? = null) {
        val updatedProduct = Product(
            id = id,
            name = name,
            quantity = quantity,
            price = price,
            imagePath = imagePath
        )
        
        val validationError = updatedProduct.getValidationError()
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.updateProduct(updatedProduct, userId)
                _errorMessage.value = null
                _syncStatus.value = "Product updated and synced"
            } catch (e: Exception) {
                _errorMessage.value = "Error updating product: ${e.message}"
            }
        }
    }
    
    /**
     * Delete a product
     */
    fun deleteProduct(id: String) {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(id)
                if (product != null) {
                    repository.deleteProduct(product)
                    _errorMessage.value = null
                    _syncStatus.value = "Product deleted and synced"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting product: ${e.message}"
            }
        }
    }
    
    /**
     * Manually sync local products to cloud
     */
    fun syncToCloud() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }
        
        viewModelScope.launch {
            try {
                _syncStatus.value = "Syncing to cloud..."
                repository.syncToCloud(userId)
                _syncStatus.value = "Sync complete"
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed: ${e.message}"
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clear sync status
     */
    fun clearSyncStatus() {
        _syncStatus.value = null
    }
}


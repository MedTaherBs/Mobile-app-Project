package com.example.projetmobile.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore service for cloud synchronization
 */
class FirestoreService {
    
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")
    
    /**
     * Get all products from Firestore as Flow (real-time updates)
     */
    fun getAllProducts(userId: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val products = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Product(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 0,
                            price = doc.getDouble("price") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(products)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Add product to Firestore
     */
    suspend fun addProduct(userId: String, product: Product): Result<Unit> {
        return try {
            val productData = hashMapOf(
                "userId" to userId,
                "name" to product.name,
                "quantity" to product.quantity,
                "price" to product.price,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            
            productsCollection.document(product.id).set(productData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update product in Firestore
     */
    suspend fun updateProduct(userId: String, product: Product): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "userId" to userId,
                "name" to product.name,
                "quantity" to product.quantity,
                "price" to product.price,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            
            productsCollection.document(product.id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete product from Firestore
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync local products to Firestore
     */
    suspend fun syncToCloud(userId: String, products: List<Product>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            products.forEach { product ->
                val productData = hashMapOf(
                    "userId" to userId,
                    "name" to product.name,
                    "quantity" to product.quantity,
                    "price" to product.price,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                batch.set(productsCollection.document(product.id), productData)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

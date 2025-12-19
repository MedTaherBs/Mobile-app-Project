package com.example.smartshop.data

/**
 * Product data model
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imagePath: String? = null
) {
    /**
     * Validate product data
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && price > 0 && quantity >= 0
    }
    
    /**
     * Get validation error message
     */
    fun getValidationError(): String? {
        return when {
            name.isBlank() -> "Product name cannot be empty"
            price <= 0 -> "Price must be greater than 0"
            quantity < 0 -> "Quantity must be greater than or equal to 0"
            else -> null
        }
    }
}

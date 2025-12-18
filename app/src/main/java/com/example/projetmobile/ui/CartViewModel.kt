package com.example.projetmobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetmobile.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Cart management
 */
class CartViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val cartRepository = CartRepository(database.cartDao(), database.productDao())
    private val orderRepository = OrderRepository(database.orderDao(), database.productDao(), database.cartDao())
    
    private val _cartState = MutableStateFlow<CartState>(CartState.Idle)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    
    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()
    
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()
    
    fun loadCart(userId: String) {
        viewModelScope.launch {
            cartRepository.getCartItems(userId).collect { items ->
                _cartState.value = CartState.Success(items)
                _cartTotal.value = items.sumOf { it.getTotalPrice() }
                _cartItemCount.value = items.size
            }
        }
    }
    
    fun addToCart(userId: String, product: Product, quantity: Int) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading
            val result = cartRepository.addToCart(userId, product, quantity)
            if (result.isFailure) {
                _cartState.value = CartState.Error(result.exceptionOrNull()?.message ?: "Failed to add to cart")
                // Reload cart to restore state
                loadCart(userId)
            }
        }
    }
    
    fun updateQuantity(itemId: String, newQuantity: Int, productId: String, userId: String) {
        viewModelScope.launch {
            val result = cartRepository.updateCartItemQuantity(itemId, newQuantity, productId)
            if (result.isFailure) {
                _cartState.value = CartState.Error(result.exceptionOrNull()?.message ?: "Failed to update quantity")
                // Reload cart
                loadCart(userId)
            }
        }
    }
    
    fun removeItem(itemId: String, userId: String) {
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(itemId)
            if (result.isFailure) {
                _cartState.value = CartState.Error(result.exceptionOrNull()?.message ?: "Failed to remove item")
            }
        }
    }
    
    fun clearCart(userId: String) {
        viewModelScope.launch {
            cartRepository.clearCart(userId)
        }
    }
    
    fun checkout(userId: String, cartItems: List<CartItem>) {
        viewModelScope.launch {
            _cartState.value = CartState.Loading
            val result = orderRepository.placeOrder(userId, cartItems)
            if (result.isSuccess) {
                _cartState.value = CartState.OrderPlaced(result.getOrNull() ?: "")
                // Cart will be automatically cleared by repository
            } else {
                _cartState.value = CartState.Error(result.exceptionOrNull()?.message ?: "Failed to place order")
                // Reload cart
                loadCart(userId)
            }
        }
    }
    
    fun resetState() {
        _cartState.value = CartState.Idle
    }
}

sealed class CartState {
    object Idle : CartState()
    object Loading : CartState()
    data class Success(val items: List<CartItem>) : CartState()
    data class Error(val message: String) : CartState()
    data class OrderPlaced(val orderId: String) : CartState()
}

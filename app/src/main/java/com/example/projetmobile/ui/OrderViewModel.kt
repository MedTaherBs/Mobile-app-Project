package com.example.projetmobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetmobile.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Order History
 */
class OrderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val orderRepository = OrderRepository(database.orderDao(), database.productDao(), database.cartDao())
    
    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()
    
    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount.asStateFlow()
    
    fun loadOrders(userId: String) {
        viewModelScope.launch {
            orderRepository.getOrders(userId).collect { orders ->
                _orderState.value = OrderState.Success(orders)
                _orderCount.value = orders.size
            }
        }
    }
    
    fun loadOrderCount(userId: String) {
        viewModelScope.launch {
            val count = orderRepository.getOrderCount(userId)
            _orderCount.value = count
        }
    }
}

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val orders: List<Order>) : OrderState()
    data class Error(val message: String) : OrderState()
}

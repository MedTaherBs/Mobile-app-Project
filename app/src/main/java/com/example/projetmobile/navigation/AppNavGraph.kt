package com.example.projetmobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projetmobile.ui.*

/**
 * Navigation routes for the app
 */
object AppDestinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SHOP = "shop"
    const val CART = "cart"
    const val ORDER_HISTORY = "orderHistory"
}

/**
 * Main navigation graph for the app.
 * Defines all navigation routes and screen transitions.
 */
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Login Screen - Start destination
        composable(route = AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to home and clear back stack
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // Product Management Screen - After successful login
        composable(route = AppDestinations.HOME) {
            ProductManagementScreen(
                onLogout = {
                    // Navigate back to login and clear back stack
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToShop = {
                    navController.navigate(AppDestinations.SHOP)
                },
                onNavigateToOrderHistory = {
                    navController.navigate(AppDestinations.ORDER_HISTORY)
                }
            )
        }
        
        // Shop Screen - Browse and add products to cart
        composable(route = AppDestinations.SHOP) {
            ShopScreen(
                userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCart = {
                    navController.navigate(AppDestinations.CART)
                }
            )
        }
        
        // Cart Screen - View and manage cart items
        composable(route = AppDestinations.CART) {
            CartScreen(
                userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOrderHistory = {
                    navController.navigate(AppDestinations.ORDER_HISTORY) {
                        popUpTo(AppDestinations.HOME) { inclusive = false }
                    }
                }
            )
        }
        
        // Order History Screen - View past orders
        composable(route = AppDestinations.ORDER_HISTORY) {
            OrderHistoryScreen(
                userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

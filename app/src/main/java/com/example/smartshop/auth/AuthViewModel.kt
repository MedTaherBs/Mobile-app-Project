package com.example.smartshop.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Authentication states
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel for authentication operations using Firebase Authentication.
 * Manages login, registration, and logout with proper state handling.
 */
class AuthViewModel : ViewModel() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // State management using StateFlow
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Check if user is already logged in
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Success(user)
        }
    }
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                result.user?.let { user ->
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    _authState.value = AuthState.Error("Login failed: User not found")
                }
                
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "An unknown error occurred during login"
                )
            }
        }
    }
    
    /**
     * Register a new user with email and password
     */
    fun register(email: String, password: String) {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                result.user?.let { user ->
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    _authState.value = AuthState.Error("Registration failed")
                }
                
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "An unknown error occurred during registration"
                )
            }
        }
    }
    
    /**
     * Logout the current user
     */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
    
    /**
     * Reset the auth state to Idle
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    /**
     * Get the current user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}

package com.example.projetmobile.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projetmobile.data.Product
import java.io.File
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    userId: String,
    onNavigateToCart: () -> Unit,
    onNavigateBack: () -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val products by productViewModel.products.collectAsState()
    val cartItemCount by cartViewModel.cartItemCount.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    
    var showAddToCartDialog by remember { mutableStateOf<Product?>(null) }
    
    LaunchedEffect(userId) {
        cartViewModel.loadCart(userId)
    }
    
    // Show snackbar for cart errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(cartState) {
        if (cartState is CartState.Error) {
            snackbarHostState.showSnackbar((cartState as CartState.Error).message)
            cartViewModel.resetState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge {
                                    Text("$cartItemCount")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (products.isEmpty()) {
                EmptyShopView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ShopProductCard(
                            product = product,
                            onAddToCart = { showAddToCartDialog = product }
                        )
                    }
                }
            }
        }
    }
    
    // Add to Cart Dialog
    showAddToCartDialog?.let { product ->
        AddToCartDialog(
            product = product,
            onDismiss = { showAddToCartDialog = null },
            onConfirm = { quantity ->
                cartViewModel.addToCart(userId, product, quantity)
                showAddToCartDialog = null
            }
        )
    }
}

@Composable
fun EmptyShopView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No products available",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Products will appear here when available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShopProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image
            product.imagePath?.let { imagePath ->
                val file = File(imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    PlaceholderImage()
                }
            } ?: PlaceholderImage()
            
            // Product Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    formatPrice(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Stock Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (product.quantity > 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    Text(
                        if (product.quantity > 0) 
                            "${product.quantity} in stock" 
                        else 
                            "Out of stock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (product.quantity > 0) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Add to Cart Button
                Button(
                    onClick = onAddToCart,
                    enabled = product.quantity > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (product.quantity > 0) "Add to Cart" else "Out of Stock")
                }
            }
        }
    }
}

@Composable
fun AddToCartDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Cart") },
        text = {
            Column {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Price: ${formatPrice(product.price)}")
                Text("Available: ${product.quantity}")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        quantity = it
                        errorMessage = null
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    leadingIcon = {
                        IconButton(onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            if (current > 1) {
                                quantity = (current - 1).toString()
                            }
                        }) {
                            Icon(Icons.Default.Remove, "Decrease")
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            if (current < product.quantity) {
                                quantity = (current + 1).toString()
                            }
                        }) {
                            Icon(Icons.Default.Add, "Increase")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull()
                    when {
                        qty == null || qty <= 0 -> {
                            errorMessage = "Please enter a valid quantity"
                        }
                        qty > product.quantity -> {
                            errorMessage = "Quantity exceeds available stock"
                        }
                        else -> {
                            onConfirm(qty)
                        }
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PlaceholderImage() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(50.dp),
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

private fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(price)
}

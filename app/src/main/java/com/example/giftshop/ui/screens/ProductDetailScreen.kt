@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.giftshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giftshop.data.ProductRepository
import com.example.giftshop.data.formatPrice
import com.example.giftshop.ui.CartViewModel
import com.example.giftshop.ui.components.ProductArtwork
import com.example.giftshop.ui.components.QuantityStepper

@Composable
fun ProductDetailScreen(
    productId: String,
    cart: CartViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
) {
    val product = ProductRepository.byId(productId)

    if (product == null) {
        NotFound(onBack = onBack)
        return
    }

    val quantity = cart.quantityOf(product.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (quantity > 0) {
                        QuantityStepper(
                            quantity = quantity,
                            onIncrement = { cart.add(product) },
                            onDecrement = { cart.decrement(product.id) },
                        )
                        Button(
                            onClick = onCartClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Zum Warenkorb")
                        }
                    } else {
                        Button(
                            onClick = { cart.add(product) },
                            modifier = Modifier
                                .weight(1f)
                                .height(ButtonDefaults.MinHeight + 8.dp),
                        ) {
                            Text("In den Warenkorb · ${formatPrice(product.priceCents)}")
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProductArtwork(
                product = product,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                emojiSize = 96.sp,
                cornerRadius = 24.dp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AssistChip(onClick = {}, label = { Text(product.category.label) })
                Text(text = product.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = product.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatPrice(product.priceCents),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalDivider()

            Text(text = "Beschreibung", style = MaterialTheme.typography.titleMedium)
            Text(text = product.description, style = MaterialTheme.typography.bodyLarge)

            Text(
                text = "Versand in 1–2 Werktagen · 30 Tage Rückgaberecht",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NotFound(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nicht gefunden") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Dieses Produkt gibt es nicht mehr.", style = MaterialTheme.typography.titleMedium)
        }
    }
}

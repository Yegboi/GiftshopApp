@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.giftshop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giftshop.data.formatPrice
import com.example.giftshop.ui.CartLine
import com.example.giftshop.ui.CartViewModel
import com.example.giftshop.ui.components.ProductArtwork
import com.example.giftshop.ui.components.QuantityStepper
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    cart: CartViewModel,
    onBack: () -> Unit,
    onContinueShopping: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warenkorb") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                        )
                    }
                },
                actions = {
                    if (!cart.isEmpty) {
                        TextButton(onClick = { cart.clear() }) { Text("Leeren") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!cart.isEmpty) {
                CheckoutBar(
                    cart = cart,
                    onCheckout = {
                        val total = formatPrice(cart.totalCents)
                        cart.clear()
                        scope.launch {
                            snackbarHostState.showSnackbar("Bestellung über $total aufgegeben")
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        if (cart.isEmpty) {
            EmptyCart(
                onContinueShopping = onContinueShopping,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cart.lines, key = { it.product.id }) { line ->
                    CartLineCard(
                        line = line,
                        onIncrement = { cart.add(line.product) },
                        onDecrement = { cart.decrement(line.product.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductArtwork(
                product = line.product,
                modifier = Modifier.size(64.dp),
                emojiSize = 28.sp,
                cornerRadius = 12.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatPrice(line.product.priceCents)} pro Stück",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatPrice(line.lineTotalCents),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            QuantityStepper(
                quantity = line.quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
            )
        }
    }
}

@Composable
private fun CheckoutBar(cart: CartViewModel, onCheckout: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SummaryRow("Zwischensumme", formatPrice(cart.subtotalCents))
            SummaryRow(
                label = "Versand",
                value = if (cart.shippingCents == 0) "gratis" else formatPrice(cart.shippingCents),
            )

            if (cart.missingForFreeShippingCents > 0) {
                Text(
                    text = "Noch ${formatPrice(cart.missingForFreeShippingCents)} " +
                        "bis zum kostenlosen Versand",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Gesamt", style = MaterialTheme.typography.titleLarge)
                Text(formatPrice(cart.totalCents), style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text("Kostenpflichtig bestellen")
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyCart(onContinueShopping: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎁", fontSize = 64.sp)
        Text(
            text = "Dein Warenkorb ist leer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Stöber im Katalog und leg etwas Schönes hinein.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        OutlinedButton(
            onClick = onContinueShopping,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text("Weiter stöbern")
        }
    }
}

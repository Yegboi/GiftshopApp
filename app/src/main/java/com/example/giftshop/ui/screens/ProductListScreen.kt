@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.giftshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giftshop.data.Category
import com.example.giftshop.data.Product
import com.example.giftshop.data.ProductRepository
import com.example.giftshop.data.formatPrice
import com.example.giftshop.ui.CartViewModel
import com.example.giftshop.ui.components.ProductArtwork

@Composable
fun ProductListScreen(
    cart: CartViewModel,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
) {
    var selectedCategory by rememberSaveable { mutableStateOf(Category.ALL) }

    val visibleProducts = if (selectedCategory == Category.ALL) {
        ProductRepository.products
    } else {
        ProductRepository.products.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giftshop") },
                actions = {
                    CartAction(itemCount = cart.itemCount, onClick = onCartClick)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Geschenke, die ankommen",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Ab ${formatPrice(CartViewModel.FREE_SHIPPING_THRESHOLD_CENTS)} versandkostenfrei",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(Category.entries.toList(), key = { it.name }) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label) },
                        )
                    }
                }
            }

            items(visibleProducts, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    quantityInCart = cart.quantityOf(product.id),
                    onClick = { onProductClick(product.id) },
                    onAdd = { cart.add(product) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProductArtwork(
                product = product,
                modifier = Modifier.size(88.dp),
                emojiSize = 38.sp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = product.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatPrice(product.priceCents),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FilledTonalButton(onClick = onAdd) {
                        Text(if (quantityInCart > 0) "Im Korb ($quantityInCart)" else "Hinzufügen")
                    }
                }
            }
        }
    }
}

/** Cart icon in the app bar with a count bubble in the top-right corner. */
@Composable
private fun CartAction(itemCount: Int, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = if (itemCount > 0) {
                    "Warenkorb, $itemCount Artikel"
                } else {
                    "Warenkorb, leer"
                },
            )
        }

        if (itemCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (itemCount > 99) "99+" else itemCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

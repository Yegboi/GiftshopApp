package com.example.giftshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giftshop.data.Product

/**
 * Stands in for a product photo: the product's accent colour with its emoji
 * centred on top. Keeps the app free of bitmap assets.
 */
@Composable
fun ProductArtwork(
    product: Product,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 44.sp,
    cornerRadius: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(product.accent)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = product.emoji, fontSize = emojiSize)
    }
}

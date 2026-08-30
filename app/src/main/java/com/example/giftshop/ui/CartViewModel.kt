package com.example.giftshop.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.example.giftshop.data.Product

/** One product plus how many of it sit in the cart. */
data class CartLine(val product: Product, val quantity: Int) {
    val lineTotalCents: Int get() = product.priceCents * quantity
}

/**
 * Holds the cart for the whole app. A single instance is scoped to the
 * activity in [GiftshopApp] so it survives navigation between destinations.
 *
 * Lines keep their own [Product] rather than an id into the catalogue, so the
 * cart stays independent of where products come from.
 */
class CartViewModel : ViewModel() {

    /** Product id -> line. Backed by a snapshot map so Compose recomposes. */
    private val entries = mutableStateMapOf<String, CartLine>()

    val lines: List<CartLine> get() = entries.values.sortedBy { it.product.name }

    val itemCount: Int get() = entries.values.sumOf { it.quantity }

    val isEmpty: Boolean get() = entries.isEmpty()

    val subtotalCents: Int get() = entries.values.sumOf { it.lineTotalCents }

    /** Free shipping once the subtotal reaches the threshold. */
    val shippingCents: Int
        get() = when {
            subtotalCents == 0 -> 0
            subtotalCents >= FREE_SHIPPING_THRESHOLD_CENTS -> 0
            else -> SHIPPING_CENTS
        }

    val totalCents: Int get() = subtotalCents + shippingCents

    /** Cents still missing for free shipping, or 0 once it is reached. */
    val missingForFreeShippingCents: Int
        get() = (FREE_SHIPPING_THRESHOLD_CENTS - subtotalCents).coerceAtLeast(0)

    fun quantityOf(productId: String): Int = entries[productId]?.quantity ?: 0

    fun add(product: Product, amount: Int = 1) {
        if (amount <= 0) return
        val next = (quantityOf(product.id) + amount).coerceAtMost(MAX_PER_PRODUCT)
        entries[product.id] = CartLine(product, next)
    }

    fun decrement(productId: String) {
        val line = entries[productId] ?: return
        if (line.quantity <= 1) {
            entries.remove(productId)
        } else {
            entries[productId] = line.copy(quantity = line.quantity - 1)
        }
    }

    fun remove(productId: String) {
        entries.remove(productId)
    }

    fun clear() {
        entries.clear()
    }

    companion object {
        const val MAX_PER_PRODUCT = 99
        const val SHIPPING_CENTS = 490
        const val FREE_SHIPPING_THRESHOLD_CENTS = 5000
    }
}

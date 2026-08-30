package com.example.giftshop.data

/** A category used to group products on the list screen. */
enum class Category(val label: String) {
    ALL("Alle"),
    HOME("Zuhause"),
    SWEETS("Naschen"),
    ACCESSORIES("Accessoires"),
    STATIONERY("Papeterie"),
}

/**
 * A single item in the shop. [accent] is a packed ARGB value used as the card
 * background so the catalogue stays colourful without shipping any bitmaps.
 */
data class Product(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val priceCents: Int,
    val emoji: String,
    val category: Category,
    val accent: Long,
)

/** Formats a cent amount as a German price string, e.g. `1290` -> `12,90 €`. */
fun formatPrice(cents: Int): String {
    val sign = if (cents < 0) "-" else ""
    val abs = kotlin.math.abs(cents)
    return "$sign${abs / 100},${(abs % 100).toString().padStart(2, '0')} €"
}

package com.example.giftshop.ui

import com.example.giftshop.data.Category
import com.example.giftshop.data.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartViewModelTest {

    private lateinit var cart: CartViewModel

    private val candle = product("candle", "Kerze", 2000)
    private val mug = product("mug", "Becher", 1500)

    private fun product(id: String, name: String, priceCents: Int) = Product(
        id = id,
        name = name,
        tagline = "",
        description = "",
        priceCents = priceCents,
        emoji = "🎁",
        category = Category.HOME,
        accent = 0xFFFFFFFF,
    )

    @Before
    fun setUp() {
        cart = CartViewModel()
    }

    @Test
    fun `starts empty`() {
        assertTrue(cart.isEmpty)
        assertEquals(0, cart.itemCount)
        assertEquals(0, cart.subtotalCents)
        assertEquals(0, cart.totalCents)
    }

    @Test
    fun `an empty cart is charged no shipping`() {
        assertEquals(0, cart.shippingCents)
    }

    @Test
    fun `adding the same product twice increments its quantity`() {
        cart.add(candle)
        cart.add(candle)

        assertEquals(2, cart.quantityOf(candle.id))
        assertEquals(1, cart.lines.size)
        assertEquals(2, cart.itemCount)
    }

    @Test
    fun `subtotal sums the lines`() {
        cart.add(candle, 2)
        cart.add(mug)

        assertEquals(2 * 2000 + 1500, cart.subtotalCents)
    }

    @Test
    fun `lines are sorted by product name`() {
        cart.add(mug)
        cart.add(candle)

        assertEquals(listOf("Becher", "Kerze"), cart.lines.map { it.product.name })
    }

    @Test
    fun `adding a non-positive amount does nothing`() {
        cart.add(candle, 0)
        cart.add(candle, -3)

        assertTrue(cart.isEmpty)
    }

    @Test
    fun `quantity is capped per product`() {
        cart.add(candle, CartViewModel.MAX_PER_PRODUCT + 50)

        assertEquals(CartViewModel.MAX_PER_PRODUCT, cart.quantityOf(candle.id))
    }

    @Test
    fun `decrementing below one drops the line`() {
        cart.add(candle)
        cart.decrement(candle.id)

        assertTrue(cart.isEmpty)
        assertEquals(0, cart.quantityOf(candle.id))
    }

    @Test
    fun `decrementing an absent product is a no-op`() {
        cart.decrement("ghost")

        assertTrue(cart.isEmpty)
    }

    @Test
    fun `remove drops the whole line regardless of quantity`() {
        cart.add(candle, 5)
        cart.remove(candle.id)

        assertTrue(cart.isEmpty)
    }

    @Test
    fun `clear empties the cart`() {
        cart.add(candle)
        cart.add(mug)
        cart.clear()

        assertTrue(cart.isEmpty)
        assertFalse(cart.lines.isNotEmpty())
    }

    @Test
    fun `shipping is charged below the free-shipping threshold`() {
        cart.add(mug) // 15,00 €

        assertEquals(CartViewModel.SHIPPING_CENTS, cart.shippingCents)
        assertEquals(1500 + CartViewModel.SHIPPING_CENTS, cart.totalCents)
    }

    @Test
    fun `shipping is free exactly at the threshold`() {
        cart.add(product("bundle", "Bundle", CartViewModel.FREE_SHIPPING_THRESHOLD_CENTS))

        assertEquals(0, cart.shippingCents)
        assertEquals(CartViewModel.FREE_SHIPPING_THRESHOLD_CENTS, cart.totalCents)
        assertEquals(0, cart.missingForFreeShippingCents)
    }

    @Test
    fun `reports how much is missing for free shipping`() {
        cart.add(mug) // 15,00 € of 50,00 €

        assertEquals(CartViewModel.FREE_SHIPPING_THRESHOLD_CENTS - 1500, cart.missingForFreeShippingCents)
    }

    @Test
    fun `line total multiplies price by quantity`() {
        cart.add(candle, 3)

        assertEquals(3 * 2000, cart.lines.single().lineTotalCents)
    }
}

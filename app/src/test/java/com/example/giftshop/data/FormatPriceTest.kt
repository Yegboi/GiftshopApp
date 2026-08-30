package com.example.giftshop.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatPriceTest {

    @Test
    fun `formats whole euros`() {
        assertEquals("12,00 €", formatPrice(1200))
    }

    @Test
    fun `pads single-digit cents`() {
        assertEquals("12,05 €", formatPrice(1205))
    }

    @Test
    fun `formats zero`() {
        assertEquals("0,00 €", formatPrice(0))
    }

    @Test
    fun `formats amounts below one euro`() {
        assertEquals("0,99 €", formatPrice(99))
    }

    @Test
    fun `keeps the sign in front for negative amounts`() {
        assertEquals("-3,50 €", formatPrice(-350))
    }
}

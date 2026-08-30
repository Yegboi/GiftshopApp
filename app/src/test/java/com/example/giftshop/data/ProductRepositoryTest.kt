package com.example.giftshop.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryTest {

    @Test
    fun `catalogue is not empty`() {
        assertTrue(ProductRepository.products.isNotEmpty())
    }

    @Test
    fun `product ids are unique`() {
        val ids = ProductRepository.products.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every product has a positive price`() {
        assertTrue(ProductRepository.products.all { it.priceCents > 0 })
    }

    @Test
    fun `no product is filed under the ALL pseudo-category`() {
        assertTrue(ProductRepository.products.none { it.category == Category.ALL })
    }

    @Test
    fun `byId finds a known product`() {
        assertNotNull(ProductRepository.byId(ProductRepository.products.first().id))
    }

    @Test
    fun `byId returns null for an unknown id`() {
        assertNull(ProductRepository.byId("does-not-exist"))
    }
}

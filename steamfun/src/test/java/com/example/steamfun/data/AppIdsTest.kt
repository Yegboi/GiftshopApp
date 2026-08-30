package com.example.steamfun.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIdsTest {

    @Test
    fun `there are enough games for a session without repeats`() {
        assertTrue("nur ${AppIds.candidates.size}", AppIds.candidates.size >= 50)
    }

    @Test
    fun `no appid appears twice`() {
        assertEquals(AppIds.candidates.size, AppIds.candidates.toSet().size)
    }

    @Test
    fun `every appid is plausible`() {
        AppIds.candidates.forEach { assertTrue("$it", it > 0) }
    }

    @Test
    fun `a pass covers every candidate exactly once`() {
        val order = AppIds.shuffledOrder()

        assertEquals(AppIds.candidates.size, order.size)
        assertEquals(AppIds.candidates.toSet(), order.toSet())
    }

    @Test
    fun `the same seed gives the same order`() {
        assertEquals(AppIds.shuffledOrder(seed = 42), AppIds.shuffledOrder(seed = 42))
    }

    @Test
    fun `shuffling actually reorders`() {
        val orders = (1L..8L).map { AppIds.shuffledOrder(seed = it) }
        assertTrue("alle Reihenfolgen identisch", orders.toSet().size > 1)
    }
}

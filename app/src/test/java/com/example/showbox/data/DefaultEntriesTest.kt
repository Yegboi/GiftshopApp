package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultEntriesTest {

    @Test
    fun `every category is seeded`() {
        Category.entries.forEach { category ->
            val seeded = DefaultEntries.all.count { it.category == category }
            assertTrue("Kategorie $category hat keine Platzhalter", seeded > 0)
        }
    }

    @Test
    fun `seeded ids are unique`() {
        val ids = DefaultEntries.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `no seeded prompt is blank`() {
        assertTrue(DefaultEntries.all.none { it.prompt.isBlank() })
    }

    @Test
    fun `categories with answers have them filled in`() {
        val missing = DefaultEntries.all.filter { it.category.hasAnswer && it.answer.isBlank() }
        assertTrue("Ohne Antwort: ${missing.map { it.prompt }}", missing.isEmpty())
    }

    @Test
    fun `categories without answers carry none`() {
        assertTrue(DefaultEntries.all.none { !it.category.hasAnswer && it.answer.isNotEmpty() })
    }
}

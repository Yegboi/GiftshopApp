package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntriesTest {

    @Test
    fun `creates an entry with trimmed text`() {
        val entry = Entries.create(Category.QUIZ, "  Wie viele?  ", "  42  ")

        assertNotNull(entry)
        assertEquals("Wie viele?", entry!!.prompt)
        assertEquals("42", entry.answer)
        assertEquals(Category.QUIZ, entry.category)
    }

    @Test
    fun `rejects a blank prompt`() {
        assertNull(Entries.create(Category.QUIZ, "   ", "42"))
        assertNull(Entries.create(Category.PODCAST, "", ""))
    }

    @Test
    fun `drops the answer for categories that have none`() {
        val speedDating = Entries.create(Category.SPEED_DATING, "Lieblingsfarbe?", "Blau")
        val podcast = Entries.create(Category.PODCAST, "Thema", "egal")

        assertEquals("", speedDating!!.answer)
        assertEquals("", podcast!!.answer)
    }

    @Test
    fun `keeps the answer for categories that have one`() {
        assertEquals("206", Entries.create(Category.ESTIMATION, "Knochen?", "206")!!.answer)
    }

    @Test
    fun `gives every entry a distinct id`() {
        val ids = (1..50).mapNotNull { Entries.create(Category.QUIZ, "Frage $it")?.id }

        assertEquals(50, ids.size)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `only quiz and estimation carry answers`() {
        assertTrue(Category.QUIZ.hasAnswer)
        assertTrue(Category.ESTIMATION.hasAnswer)
        assertTrue(!Category.SPEED_DATING.hasAnswer)
        assertTrue(!Category.PODCAST.hasAnswer)
    }
}

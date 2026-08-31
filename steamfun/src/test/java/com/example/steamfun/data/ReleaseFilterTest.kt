package com.example.steamfun.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseFilterTest {

    private val now = 2026

    private fun released(date: String, comingSoon: Boolean = false) =
        ReleaseFilter.isReleased(comingSoon, date, now)

    @Test
    fun `a game with a past date counts as released`() {
        assertTrue(released("10 Oct, 2007"))
        assertTrue(released("21 Aug, 2012"))
        assertTrue(released("1998"))
    }

    @Test
    fun `this year counts as released`() {
        assertTrue(released("3 Mar, 2026"))
    }

    @Test
    fun `a date next year does not`() {
        // The complaint that started this: nothing but 2027 titles with no reviews.
        assertFalse(released("2027"))
        assertFalse(released("Q4 2027"))
        assertFalse(released("15 Jun, 2028"))
    }

    @Test
    fun `steams own coming soon flag overrules any date`() {
        assertFalse(released("10 Oct, 2007", comingSoon = true))
        assertFalse(released("2026", comingSoon = true))
    }

    @Test
    fun `a date with no year at all is treated as unreleased`() {
        listOf("Coming soon", "To be announced", "Demnächst", "", "   ", "TBA").forEach {
            assertFalse("bei <$it>", released(it))
        }
    }

    @Test
    fun `a quarter or month without a year is not enough`() {
        assertFalse(released("Q1"))
        assertFalse(released("March"))
    }

    @Test
    fun `only a plausible four digit year is read`() {
        // Version numbers and the like must not be mistaken for a release year.
        assertFalse(released("Build 1234"))
        assertTrue(released("Version 2 — 1999"))
    }

    @Test
    fun `the first year in the text decides`() {
        assertTrue(released("12 Dec, 2014 (Remaster 2027)"))
    }
}

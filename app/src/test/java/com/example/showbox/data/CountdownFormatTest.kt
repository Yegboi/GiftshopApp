package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class CountdownFormatTest {

    @Test
    fun `formats hours minutes and seconds`() {
        assertEquals("00:00:00", formatCountdown(Duration.ZERO))
        assertEquals("00:00:09", formatCountdown(Duration.ofSeconds(9)))
        assertEquals("01:30:00", formatCountdown(Duration.ofMinutes(90)))
        assertEquals("23:59:59", formatCountdown(Duration.ofSeconds(86_399)))
    }

    @Test
    fun `prefixes whole days`() {
        assertEquals("1 Tag, 00:00:00", formatCountdown(Duration.ofDays(1)))
        assertEquals("2 Tage, 03:04:05", formatCountdown(Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(5)))
    }

    @Test
    fun `a negative duration reads as zero`() {
        assertEquals("00:00:00", formatCountdown(Duration.ofSeconds(-30)))
    }

    @Test
    fun `clock wraps times past midnight`() {
        assertEquals("00:00", clock(0))
        assertEquals("12:00", clock(12 * 60))
        assertEquals("02:00", clock(26 * 60))
        assertEquals("23:59", clock(24 * 60 - 1))
    }

    @Test
    fun `the default festival start is the next thursday`() {
        var day = LocalDate.of(2026, 1, 1)
        repeat(400) {
            val result = defaultFestivalStart(day)

            assertEquals(DayOfWeek.THURSDAY, result.dayOfWeek)
            assertTrue("$result liegt vor $day", !result.isBefore(day))
            assertTrue("$result ist mehr als eine Woche entfernt", result.isBefore(day.plusDays(7)))
            day = day.plusDays(1)
        }
    }

    @Test
    fun `a thursday maps to itself`() {
        val thursday = defaultFestivalStart(LocalDate.of(2026, 1, 1))
        assertEquals(thursday, defaultFestivalStart(thursday))
    }
}

package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTest {

    @Test
    fun `clamp keeps values inside the allowed range`() {
        assertEquals(SpeedRange.MIN, SpeedRange.clamp(0.1f), 0.001f)
        assertEquals(SpeedRange.MAX, SpeedRange.clamp(5f), 0.001f)
        assertEquals(1.25f, SpeedRange.clamp(1.25f), 0.001f)
    }

    @Test
    fun `clamp accepts the boundaries themselves`() {
        assertEquals(SpeedRange.MIN, SpeedRange.clamp(SpeedRange.MIN), 0.001f)
        assertEquals(SpeedRange.MAX, SpeedRange.clamp(SpeedRange.MAX), 0.001f)
    }

    @Test
    fun `clamp falls back to normal speed for NaN`() {
        assertEquals(SpeedRange.DEFAULT, SpeedRange.clamp(Float.NaN), 0.001f)
    }

    @Test
    fun `default speed lies inside the range`() {
        assertEquals(SpeedRange.DEFAULT, SpeedRange.clamp(SpeedRange.DEFAULT), 0.001f)
    }

    @Test
    fun `speed is formatted with one decimal and a german comma`() {
        assertEquals("1,0x", formatSpeed(1.0f))
        assertEquals("0,5x", formatSpeed(0.5f))
        assertEquals("2,0x", formatSpeed(2.0f))
    }

    @Test
    fun `duration is formatted as minutes and padded seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:07", formatDuration(7_000))
        assertEquals("1:00", formatDuration(60_000))
        assertEquals("3:45", formatDuration(225_000))
    }

    @Test
    fun `negative positions are clamped to zero`() {
        assertEquals("0:00", formatDuration(-5_000))
    }
}

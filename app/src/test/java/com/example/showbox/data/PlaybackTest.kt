package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackTest {

    private val eps = 0.001f

    @Test
    fun `the range reaches five percent of normal speed`() {
        assertEquals(0.05f, SpeedRange.MIN, eps)
        assertEquals(2.0f, SpeedRange.MAX, eps)
    }

    @Test
    fun `clamp keeps values inside the allowed range`() {
        assertEquals(SpeedRange.MIN, SpeedRange.clamp(0.001f), eps)
        assertEquals(SpeedRange.MAX, SpeedRange.clamp(5f), eps)
        assertEquals(0.1f, SpeedRange.clamp(0.1f), eps)
        assertEquals(1.25f, SpeedRange.clamp(1.25f), eps)
    }

    @Test
    fun `clamp accepts the boundaries themselves`() {
        assertEquals(SpeedRange.MIN, SpeedRange.clamp(SpeedRange.MIN), eps)
        assertEquals(SpeedRange.MAX, SpeedRange.clamp(SpeedRange.MAX), eps)
    }

    @Test
    fun `clamp falls back to normal speed for NaN`() {
        assertEquals(SpeedRange.DEFAULT, SpeedRange.clamp(Float.NaN), eps)
    }

    @Test
    fun `the slider ends map to the range ends`() {
        assertEquals(0f, speedToSlider(SpeedRange.MIN), eps)
        assertEquals(1f, speedToSlider(SpeedRange.MAX), eps)
        assertEquals(SpeedRange.MIN, sliderToSpeed(0f), eps)
        assertEquals(SpeedRange.MAX, sliderToSpeed(1f), eps)
    }

    @Test
    fun `slider positions outside 0 to 1 are clamped`() {
        assertEquals(SpeedRange.MIN, sliderToSpeed(-2f), eps)
        assertEquals(SpeedRange.MAX, sliderToSpeed(9f), eps)
    }

    @Test
    fun `slider mapping round-trips within one quantisation step`() {
        listOf(0.05f, 0.07f, 0.1f, 0.25f, 0.5f, 0.8f, 1.0f, 1.5f, 2.0f).forEach { speed ->
            val back = sliderToSpeed(speedToSlider(speed))
            assertTrue("$speed kam als $back zurück", kotlin.math.abs(back - speed) <= 0.051f)
        }
    }

    @Test
    fun `the slider rises monotonically with speed`() {
        var previous = -1f
        var speed = SpeedRange.MIN
        while (speed <= SpeedRange.MAX) {
            val position = speedToSlider(speed)
            assertTrue("bei $speed", position > previous)
            previous = position
            speed += 0.01f
        }
    }

    @Test
    fun `normal speed sits in the upper part of the track`() {
        // Most of the slider is the slow range, which is the point of the log scale.
        val position = speedToSlider(SpeedRange.DEFAULT)
        assertTrue("1,0x lag bei $position", position in 0.75f..0.87f)
    }

    @Test
    fun `quantisation is finer at the slow end`() {
        assertEquals(0.05f, quantizeSpeed(0.052f), eps)
        assertEquals(0.07f, quantizeSpeed(0.068f), eps)
        assertEquals(0.25f, quantizeSpeed(0.24f), eps)
        assertEquals(1.3f, quantizeSpeed(1.28f), eps)
    }

    @Test
    fun `quantisation never leaves the allowed range`() {
        var position = 0f
        while (position <= 1f) {
            val speed = sliderToSpeed(position)
            assertTrue("$speed außerhalb", speed >= SpeedRange.MIN && speed <= SpeedRange.MAX)
            position += 0.005f
        }
    }

    @Test
    fun `slow speeds are shown with two decimals`() {
        // One decimal would round 0.05x to "0,1x" and claim twice the tempo.
        assertEquals("0,05x", formatSpeed(0.05f))
        assertEquals("0,07x", formatSpeed(0.07f))
        assertEquals("0,25x", formatSpeed(0.25f))
    }

    @Test
    fun `normal and fast speeds keep one decimal`() {
        assertEquals("0,5x", formatSpeed(0.5f))
        assertEquals("1,0x", formatSpeed(1.0f))
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

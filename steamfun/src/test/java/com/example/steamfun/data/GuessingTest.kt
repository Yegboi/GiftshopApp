package com.example.steamfun.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuessingTest {

    @Test
    fun `an exact guess always counts`() {
        listOf(0, 1, 7, 500, 143_271).forEach {
            assertTrue("$it", Guessing.accurateHit(it, it))
        }
    }

    @Test
    fun `a guess inside the tolerance counts`() {
        // 25% of 1000 is 250
        assertTrue(Guessing.accurateHit(1_250, 1_000))
        assertTrue(Guessing.accurateHit(750, 1_000))
    }

    @Test
    fun `a guess outside the tolerance does not`() {
        assertFalse(Guessing.accurateHit(1_251, 1_000))
        assertFalse(Guessing.accurateHit(749, 1_000))
    }

    @Test
    fun `tiny counts stay winnable through the minimum margin`() {
        // 25% of 3 is under one review, so the floor takes over.
        assertTrue(Guessing.accurateHit(1, 3))
        assertTrue(Guessing.accurateHit(5, 3))
        assertFalse(Guessing.accurateHit(6, 3))
    }

    @Test
    fun `a game with no reviews is guessable`() {
        assertTrue(Guessing.accurateHit(0, 0))
        assertTrue(Guessing.accurateHit(2, 0))
        assertFalse(Guessing.accurateHit(3, 0))
    }

    @Test
    fun `a negative guess never counts`() {
        assertFalse(Guessing.accurateHit(-1, 100))
    }

    @Test
    fun `a huge guess against a huge count does not overflow`() {
        assertFalse(Guessing.accurateHit(Int.MAX_VALUE, 1))
        assertTrue(Guessing.accurateHit(Int.MAX_VALUE, Int.MAX_VALUE))
    }

    @Test
    fun `the roundabout hit is the bucket the count falls into`() {
        assertTrue(Guessing.roundaboutHit(ReviewBucket.UP_TO_500, 250))
        assertFalse(Guessing.roundaboutHit(ReviewBucket.UP_TO_500, 501))
        assertTrue(Guessing.roundaboutHit(ReviewBucket.ABOVE_5000, 2_000_000))
    }

    @Test
    fun `deviation is reported in percent`() {
        assertEquals(0, Guessing.deviationPercent(100, 100))
        assertEquals(50, Guessing.deviationPercent(150, 100))
        assertEquals(50, Guessing.deviationPercent(50, 100))
        assertEquals(900, Guessing.deviationPercent(1_000, 100))
    }

    @Test
    fun `deviation is undefined without a count to compare against`() {
        assertNull(Guessing.deviationPercent(5, 0))
        assertNull(Guessing.deviationPercent(-1, 100))
    }

    @Test
    fun `typed input is read as a number`() {
        assertEquals(1234, Guessing.parseGuess("1234"))
        assertEquals(1234, Guessing.parseGuess("1.234"))
        assertEquals(1234, Guessing.parseGuess(" 1 234 "))
        assertEquals(0, Guessing.parseGuess("0"))
    }

    @Test
    fun `unusable input is rejected instead of guessed at`() {
        assertNull(Guessing.parseGuess(""))
        assertNull(Guessing.parseGuess("   "))
        assertNull(Guessing.parseGuess("keine Ahnung"))
        assertNull(Guessing.parseGuess("9999999999"))
    }

    @Test
    fun `both modes are offered`() {
        assertEquals(listOf("Accurate", "Roundabout"), GuessMode.entries.map { it.label })
    }
}

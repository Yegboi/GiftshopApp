package com.example.steamfun.data

import kotlin.math.abs
import kotlin.math.roundToInt

/** How the player commits a guess. */
enum class GuessMode(val label: String) {
    ACCURATE("Accurate"),
    ROUNDABOUT("Roundabout"),
}

object Guessing {

    /**
     * Accurate mode cannot demand the exact number — nobody guesses 143.271 on
     * the nose, and the confetti would never fire. A guess counts when it is
     * within this share of the real count.
     */
    const val TOLERANCE = 0.25

    /**
     * Floor for the tolerance, so games with a handful of reviews stay winnable:
     * with 3 reviews, 25% is less than one review.
     */
    const val MIN_MARGIN = 2.0

    /** True when [guess] is close enough to [actual] to count as a hit. */
    fun accurateHit(guess: Int, actual: Int): Boolean {
        if (guess < 0) return false
        val margin = maxOf(abs(actual) * TOLERANCE, MIN_MARGIN)
        return abs(guess.toLong() - actual.toLong()) <= margin
    }

    /** True when the chosen bucket is the one the real count falls into. */
    fun roundaboutHit(guess: ReviewBucket, actual: Int): Boolean =
        ReviewBucket.of(actual) == guess

    /**
     * How far the guess was off, in percent of the real count, for the result
     * line. Returns null when there is nothing meaningful to compare against.
     */
    fun deviationPercent(guess: Int, actual: Int): Int? {
        if (actual <= 0 || guess < 0) return null
        return (abs(guess.toLong() - actual.toLong()) * 100.0 / actual).roundToInt()
    }

    /** Parses what the player typed; null when it is not a usable number. */
    fun parseGuess(input: String): Int? {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty() || digits.length > 9) return null
        return digits.toIntOrNull()
    }
}

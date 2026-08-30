package com.example.steamfun.data

import java.util.Locale

/**
 * The six ranges of the roundabout mode.
 *
 * The labels on the buttons share their endpoints (0–10 and 10–100 both name
 * 10), so each bucket claims its upper bound and the next one starts above it.
 * That way every review count belongs to exactly one bucket.
 */
enum class ReviewBucket(val label: String, val from: Int, val to: Int) {
    UP_TO_10("0 – 10", 0, 10),
    UP_TO_100("10 – 100", 11, 100),
    UP_TO_500("100 – 500", 101, 500),
    UP_TO_1000("500 – 1.000", 501, 1_000),
    UP_TO_5000("1.000 – 5.000", 1_001, 5_000),
    ABOVE_5000("5.000+", 5_001, Int.MAX_VALUE);

    operator fun contains(reviews: Int): Boolean = reviews in from..to

    companion object {
        /** The bucket a review count falls into. Negative counts cannot occur. */
        fun of(reviews: Int): ReviewBucket {
            val safe = reviews.coerceAtLeast(0)
            return entries.first { safe in it }
        }
    }
}

/** Thousands separator as used in German, e.g. `12345` -> `12.345`. */
fun formatCount(count: Int): String = String.format(Locale.GERMAN, "%,d", count)

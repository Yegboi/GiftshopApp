package com.example.steamfun.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewBucketTest {

    @Test
    fun `there are six buttons`() {
        assertEquals(6, ReviewBucket.entries.size)
    }

    @Test
    fun `the buckets carry the labels from the brief`() {
        assertEquals(
            listOf("0 – 10", "10 – 100", "100 – 500", "500 – 1.000", "1.000 – 5.000", "5.000+"),
            ReviewBucket.entries.map { it.label },
        )
    }

    @Test
    fun `every count belongs to exactly one bucket`() {
        val samples = listOf(0, 1, 9, 10, 11, 99, 100, 101, 499, 500, 501,
                             999, 1_000, 1_001, 4_999, 5_000, 5_001, 1_000_000)
        samples.forEach { count ->
            val matches = ReviewBucket.entries.filter { count in it }
            assertEquals("$count passte in $matches", 1, matches.size)
        }
    }

    @Test
    fun `shared endpoints go to the lower bucket`() {
        // The labels overlap at 10, 100, 500, 1000 and 5000; each belongs below.
        assertEquals(ReviewBucket.UP_TO_10, ReviewBucket.of(10))
        assertEquals(ReviewBucket.UP_TO_100, ReviewBucket.of(100))
        assertEquals(ReviewBucket.UP_TO_500, ReviewBucket.of(500))
        assertEquals(ReviewBucket.UP_TO_1000, ReviewBucket.of(1_000))
        assertEquals(ReviewBucket.UP_TO_5000, ReviewBucket.of(5_000))
    }

    @Test
    fun `one above an endpoint moves up a bucket`() {
        assertEquals(ReviewBucket.UP_TO_100, ReviewBucket.of(11))
        assertEquals(ReviewBucket.UP_TO_500, ReviewBucket.of(101))
        assertEquals(ReviewBucket.UP_TO_1000, ReviewBucket.of(501))
        assertEquals(ReviewBucket.UP_TO_5000, ReviewBucket.of(1_001))
        assertEquals(ReviewBucket.ABOVE_5000, ReviewBucket.of(5_001))
    }

    @Test
    fun `the buckets are contiguous with no gaps`() {
        ReviewBucket.entries.zipWithNext { lower, upper ->
            assertEquals("Lücke nach ${lower.label}", lower.to + 1, upper.from)
        }
    }

    @Test
    fun `zero and huge counts are covered`() {
        assertEquals(ReviewBucket.UP_TO_10, ReviewBucket.of(0))
        assertEquals(ReviewBucket.ABOVE_5000, ReviewBucket.of(Int.MAX_VALUE))
    }

    @Test
    fun `a negative count cannot happen but does not crash`() {
        assertEquals(ReviewBucket.UP_TO_10, ReviewBucket.of(-5))
    }

    @Test
    fun `counts are grouped for reading`() {
        assertEquals("0", formatCount(0))
        assertEquals("999", formatCount(999))
        assertEquals("1.000", formatCount(1_000))
        assertEquals("1.234.567", formatCount(1_234_567))
    }

    @Test
    fun `every bucket label matches its own range`() {
        ReviewBucket.entries.forEach { bucket ->
            assertTrue("${bucket.label} enthält seine Untergrenze nicht", bucket.from in bucket)
            assertEquals(bucket, ReviewBucket.of(bucket.from))
        }
    }
}

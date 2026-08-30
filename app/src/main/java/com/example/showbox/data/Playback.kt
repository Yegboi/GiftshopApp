package com.example.showbox.data

import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt

/** Allowed playback speeds for the music player. */
object SpeedRange {
    /** Five percent of normal — slow enough to pick a track apart. */
    const val MIN = 0.05f
    const val MAX = 2.0f
    const val DEFAULT = 1.0f

    fun clamp(value: Float): Float = when {
        value.isNaN() -> DEFAULT
        else -> value.coerceIn(MIN, MAX)
    }
}

/**
 * The slider runs 0..1 on a logarithmic scale.
 *
 * Linear would squeeze 0.05x–0.5x — the range this is for — into an eighth of
 * the track, where every pixel changes the tempo by a factor, while the top
 * half would crawl in 2.5% increments. On a log scale each pixel is roughly
 * the same proportional change, and 1.0x lands near 81% of the way along.
 */
fun speedToSlider(speed: Float): Float {
    val span = ln(SpeedRange.MAX / SpeedRange.MIN)
    return (ln(SpeedRange.clamp(speed) / SpeedRange.MIN) / span).coerceIn(0f, 1f)
}

/** Inverse of [speedToSlider], snapped to a readable step. */
fun sliderToSpeed(position: Float): Float {
    val span = ln(SpeedRange.MAX / SpeedRange.MIN)
    return quantizeSpeed(SpeedRange.MIN * exp(position.coerceIn(0f, 1f) * span))
}

/** Snaps to a step the user can read: finer when slow, coarser when fast. */
fun quantizeSpeed(speed: Float): Float {
    val clamped = SpeedRange.clamp(speed)
    val step = when {
        clamped < 0.1f -> 0.01f
        clamped < 0.5f -> 0.05f
        else -> 0.1f
    }
    return SpeedRange.clamp((clamped / step).roundToInt() * step)
}

/**
 * Formats a speed factor. Below 0.5x it needs two decimals — one would round
 * 0.05x to "0,1x" and claim twice the actual tempo.
 */
fun formatSpeed(speed: Float): String =
    String.format(Locale.GERMAN, if (speed < 0.5f) "%.2fx" else "%.1fx", speed)

/** Formats a millisecond position as `m:ss`, clamped at zero. */
fun formatDuration(ms: Int): String {
    val totalSeconds = (ms.coerceAtLeast(0)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

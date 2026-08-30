package com.example.showbox.data

import java.util.Locale

/** Allowed playback speeds for the music player. */
object SpeedRange {
    const val MIN = 0.5f
    const val MAX = 2.0f
    const val DEFAULT = 1.0f

    /** Number of discrete 0.1 stops between [MIN] and [MAX], for the slider. */
    const val SLIDER_STEPS = 14

    fun clamp(value: Float): Float = when {
        value.isNaN() -> DEFAULT
        else -> value.coerceIn(MIN, MAX)
    }
}

/** Formats a speed factor for display, e.g. `1.25f` -> `1,3x`. */
fun formatSpeed(speed: Float): String = String.format(Locale.GERMAN, "%.1fx", speed)

/** Formats a millisecond position as `m:ss`, clamped at zero. */
fun formatDuration(ms: Int): String {
    val totalSeconds = (ms.coerceAtLeast(0)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

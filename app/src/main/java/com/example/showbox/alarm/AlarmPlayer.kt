package com.example.showbox.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Plays the device alarm tone plus a vibration pattern while the shift-over
 * banner is on screen. Everything is best-effort: a device without a vibrator
 * or without an alarm tone must not crash the app.
 */
class AlarmPlayer(private val context: Context) {

    private var ringtone: Ringtone? = null

    fun start() {
        stop()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (uri != null) {
            ringtone = runCatching {
                RingtoneManager.getRingtone(context, uri)?.apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
                    play()
                }
            }.getOrNull()
        }

        runCatching {
            vibrator()?.vibrate(
                VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX),
            )
        }
    }

    fun stop() {
        ringtone?.let { runCatching { it.stop() } }
        ringtone = null
        runCatching { vibrator()?.cancel() }
    }

    private fun vibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    private companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 600, 400)
        const val VIBRATION_REPEAT_INDEX = 0
    }
}

package com.example.showbox.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.showbox.data.Song
import com.example.showbox.data.SpeedRange
import com.example.showbox.data.formatSpeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wraps a [MediaPlayer] for one song at a time. Playback speed is applied via
 * `PlaybackParams`, which keeps the original pitch.
 */
class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private var player: MediaPlayer? = null

    /** MediaPlayer only accepts most calls once prepared; guards every access. */
    private var prepared = false

    /** Last speed the device actually accepted, to fall back on if it rejects one. */
    private var lastAppliedSpeed = SpeedRange.DEFAULT

    var currentSong by mutableStateOf<Song?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var speed by mutableFloatStateOf(SpeedRange.DEFAULT)
        private set

    var positionMs by mutableIntStateOf(0)
        private set

    var durationMs by mutableIntStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                val mp = player
                if (mp != null && prepared && isPlaying) {
                    positionMs = runCatching { mp.currentPosition }.getOrDefault(positionMs)
                }
            }
        }
    }

    fun play(song: Song) {
        release()
        errorMessage = null
        currentSong = song

        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build(),
            )
            mp.setDataSource(getApplication(), Uri.parse(song.uri))

            mp.setOnPreparedListener { ready ->
                prepared = true
                durationMs = runCatching { ready.duration }.getOrDefault(0)
                if (!applySpeed(ready, speed, shouldPlay = true)) {
                    speed = SpeedRange.DEFAULT
                    applySpeed(ready, SpeedRange.DEFAULT, shouldPlay = true)
                }
                lastAppliedSpeed = speed
                isPlaying = true
            }
            mp.setOnCompletionListener {
                isPlaying = false
                positionMs = durationMs
            }
            mp.setOnErrorListener { _, _, _ ->
                errorMessage = "„${song.title}“ konnte nicht abgespielt werden."
                release()
                currentSong = null
                true
            }

            player = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            // Missing file, revoked URI permission, unsupported format.
            errorMessage = "„${song.title}“ konnte nicht geöffnet werden."
            runCatching { mp.release() }
            player = null
            prepared = false
            isPlaying = false
            currentSong = null
        }
    }

    fun togglePlayPause() {
        val mp = player ?: return
        if (!prepared) return
        try {
            if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
            } else {
                // A refused speed leaves the player paused, so do not claim otherwise.
                isPlaying = applySpeed(mp, speed, shouldPlay = true)
            }
        } catch (e: IllegalStateException) {
            errorMessage = "Wiedergabe nicht möglich."
        }
    }

    fun changeSpeed(value: Float) {
        val clamped = SpeedRange.clamp(value)
        val mp = player
        if (mp == null || !prepared) {
            // Nothing loaded yet: remember it and apply when a song starts.
            speed = clamped
            return
        }
        if (applySpeed(mp, clamped, shouldPlay = isPlaying)) {
            speed = clamped
            lastAppliedSpeed = clamped
        } else {
            // The device kept the old rate, so the slider must snap back to it.
            speed = lastAppliedSpeed
        }
    }

    fun resetSpeed() = changeSpeed(SpeedRange.DEFAULT)

    fun seekTo(ms: Int) {
        val mp = player ?: return
        if (!prepared) return
        val target = ms.coerceIn(0, durationMs.coerceAtLeast(0))
        runCatching { mp.seekTo(target) }
        positionMs = target
    }

    fun dismissError() {
        errorMessage = null
    }

    /** Stops playback and forgets the current song. */
    fun stop() {
        release()
        currentSong = null
    }

    /**
     * Applies [value] and restores the intended play/pause state, because
     * `setPlaybackParams` resumes a paused player as a side effect.
     *
     * Returns false when the device refused the rate.
     */
    private fun applySpeed(mp: MediaPlayer, value: Float, shouldPlay: Boolean): Boolean =
        try {
            mp.playbackParams = mp.playbackParams.setSpeed(value)
            if (shouldPlay && !mp.isPlaying) mp.start()
            if (!shouldPlay && mp.isPlaying) mp.pause()
            true
        } catch (e: IllegalStateException) {
            errorMessage = "Geschwindigkeit konnte nicht gesetzt werden."
            false
        } catch (e: IllegalArgumentException) {
            // Extreme rates are decoder dependent; not every device goes this low.
            errorMessage = "${formatSpeed(value)} schafft dieses Gerät nicht."
            false
        }

    private fun release() {
        player?.let { mp ->
            runCatching { mp.reset() }
            runCatching { mp.release() }
        }
        player = null
        prepared = false
        isPlaying = false
        positionMs = 0
        durationMs = 0
    }

    override fun onCleared() {
        release()
        super.onCleared()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 250L
    }
}

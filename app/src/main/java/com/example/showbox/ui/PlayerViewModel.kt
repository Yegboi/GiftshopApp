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

                val accepted = applyRate(ready, speed, shouldPlay = true)
                if (accepted != null) {
                    speed = accepted
                    isPlaying = true
                } else {
                    // No rate could be set at all; start at normal speed.
                    speed = SpeedRange.DEFAULT
                    isPlaying = applyRate(ready, SpeedRange.DEFAULT, shouldPlay = true) != null
                }
                lastAppliedSpeed = speed
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
                // A refused rate leaves the player paused, so do not claim otherwise.
                isPlaying = applyRate(mp, speed, shouldPlay = true) != null
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
        val accepted = applyRate(mp, clamped, shouldPlay = isPlaying)
        if (accepted == null) {
            // The device kept the old rate, so the slider must snap back to it.
            speed = lastAppliedSpeed
            errorMessage = "Geschwindigkeit konnte nicht gesetzt werden."
            return
        }

        speed = accepted
        lastAppliedSpeed = accepted
        if (accepted > clamped + RATE_EPSILON) {
            errorMessage = "Langsamer als ${formatSpeed(accepted)} kann dieses Gerät nicht."
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
     * Slows down the way a record player does, and returns the rate the device
     * accepted — or null if no rate could be set.
     *
     * Pitch is set to the same factor as speed on purpose. Speed alone drives
     * the time-stretcher, which has to invent twenty times the material at
     * 0.05x and comes out chopped up. With pitch matching speed the stretch
     * ratio becomes 1, the stretcher drops out, and plain resampling is left:
     * slower and correspondingly deeper, the way a record sounds spun down.
     *
     * Also restores the intended play/pause state, because setPlaybackParams
     * resumes a paused player as a side effect.
     *
     * Resamplers have a floor, and it is not the same on every device, so a
     * refused rate is retried a little closer to normal until one sticks. The
     * slider then lands on the slowest this particular device manages instead
     * of refusing outright.
     */
    private fun applyRate(mp: MediaPlayer, target: Float, shouldPlay: Boolean): Float? {
        var candidate = target
        repeat(RATE_FALLBACK_TRIES) {
            try {
                mp.playbackParams = mp.playbackParams.setSpeed(candidate).setPitch(candidate)
                if (shouldPlay && !mp.isPlaying) mp.start()
                if (!shouldPlay && mp.isPlaying) mp.pause()
                return candidate
            } catch (e: IllegalArgumentException) {
                // Outside the resampler's range; step toward normal and retry.
                candidate = (candidate * RATE_FALLBACK_FACTOR).coerceAtMost(SpeedRange.MAX)
            } catch (e: IllegalStateException) {
                // The player cannot accept any rate right now.
                return null
            }
        }
        return null
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

        /** How far each retry moves toward normal speed when a rate is refused. */
        const val RATE_FALLBACK_FACTOR = 1.3f
        const val RATE_FALLBACK_TRIES = 8
        const val RATE_EPSILON = 0.001f
    }
}

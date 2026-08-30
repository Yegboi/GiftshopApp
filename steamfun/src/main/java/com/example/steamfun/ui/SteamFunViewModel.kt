package com.example.steamfun.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.steamfun.data.AppIdCache
import com.example.steamfun.data.AppIds
import com.example.steamfun.data.GuessMode
import com.example.steamfun.data.Guessing
import com.example.steamfun.data.ReviewBucket
import com.example.steamfun.data.SteamApi
import com.example.steamfun.data.SteamGame
import com.example.steamfun.data.formatCount
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

/**
 * What the screen shows right now.
 *
 * The review count rides along inside [SteamGame] from the first state, but
 * only [Answered] is ever allowed to render it — that is the whole game.
 */
sealed interface RoundState {
    data class Loading(val message: String) : RoundState

    data class Failed(val message: String) : RoundState

    /** A store page is up and the guess is still open. */
    data class Asking(val game: SteamGame) : RoundState

    data class Answered(
        val game: SteamGame,
        val correct: Boolean,
        val guessLabel: String,
        val deviationPercent: Int?,
    ) : RoundState
}

class SteamFunViewModel(app: Application) : AndroidViewModel(app) {

    private val api = SteamApi()
    private val cache = AppIdCache(File(app.filesDir, "steam-appids.bin"))

    /** Every appid on Steam, so a draw is not limited to games anyone has heard of. */
    private var catalogue: IntArray = IntArray(0)

    private var loadJob: Job? = null

    var mode by mutableStateOf(GuessMode.ROUNDABOUT)
        private set

    var round by mutableStateOf<RoundState>(RoundState.Loading("Wird geladen …"))
        private set

    var typedGuess by mutableStateOf("")
        private set

    var correctCount by mutableIntStateOf(0)
        private set

    var playedCount by mutableIntStateOf(0)
        private set

    /** How many appids the catalogue holds, for the footer line. */
    var catalogueSize by mutableIntStateOf(0)
        private set

    init {
        nextGame()
    }

    fun selectMode(newMode: GuessMode) {
        if (newMode == mode) return
        mode = newMode
        typedGuess = ""
    }

    fun onGuessTyped(text: String) {
        typedGuess = text.filter { it.isDigit() }.take(MAX_GUESS_DIGITS)
    }

    val canSubmitTyped: Boolean get() = Guessing.parseGuess(typedGuess) != null

    fun nextGame() {
        typedGuess = ""
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (catalogue.isEmpty()) {
                round = RoundState.Loading("Steam-Katalog wird geladen — das passiert nur einmal.")
                loadCatalogue()
            }
            if (catalogue.isEmpty()) {
                round = RoundState.Failed(
                    "Der Steam-Katalog liess sich nicht laden. Prüf die Internetverbindung.",
                )
                return@launch
            }

            // Most appids are DLC, soundtracks or long delisted, so a draw takes
            // a few tries. Each one costs a single request.
            repeat(MAX_ATTEMPTS) { attempt ->
                round = RoundState.Loading("Zufälliges Spiel wird gesucht … (${attempt + 1})")
                val game = api.loadGame(catalogue[Random.nextInt(catalogue.size)])
                if (game != null) {
                    round = RoundState.Asking(game)
                    return@launch
                }
            }
            round = RoundState.Failed(
                "In $MAX_ATTEMPTS Anläufen kein Spiel gefunden. Steam drosselt womöglich gerade.",
            )
        }
    }

    private suspend fun loadCatalogue() {
        cache.load(CACHE_MAX_AGE_MILLIS)?.let {
            catalogue = it
            catalogueSize = it.size
            return
        }
        val downloaded = api.downloadAppIds()
        catalogue = when {
            downloaded != null -> {
                cache.save(downloaded)
                downloaded
            }
            // Without the catalogue the game still runs, just on the seed list.
            else -> AppIds.candidates.toIntArray()
        }
        catalogueSize = catalogue.size
    }

    fun submitTypedGuess() {
        val asking = round as? RoundState.Asking ?: return
        val guess = Guessing.parseGuess(typedGuess) ?: return
        val actual = asking.game.totalReviews
        finish(
            game = asking.game,
            correct = Guessing.accurateHit(guess, actual),
            guessLabel = formatCount(guess),
            deviation = Guessing.deviationPercent(guess, actual),
        )
    }

    fun submitBucket(bucket: ReviewBucket) {
        val asking = round as? RoundState.Asking ?: return
        finish(
            game = asking.game,
            correct = Guessing.roundaboutHit(bucket, asking.game.totalReviews),
            guessLabel = bucket.label,
            deviation = null,
        )
    }

    private fun finish(game: SteamGame, correct: Boolean, guessLabel: String, deviation: Int?) {
        playedCount++
        if (correct) correctCount++
        round = RoundState.Answered(
            game = game,
            correct = correct,
            guessLabel = guessLabel,
            deviationPercent = deviation,
        )
    }

    private companion object {
        /** Appids that are DLC or unreachable are skipped; this bounds the retrying. */
        const val MAX_ATTEMPTS = 25
        const val MAX_GUESS_DIGITS = 9
        const val CACHE_MAX_AGE_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}

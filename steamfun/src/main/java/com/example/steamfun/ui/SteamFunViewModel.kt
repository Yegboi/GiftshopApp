package com.example.steamfun.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.steamfun.data.AppIds
import com.example.steamfun.data.GuessMode
import com.example.steamfun.data.Guessing
import com.example.steamfun.data.ReviewBucket
import com.example.steamfun.data.SteamApi
import com.example.steamfun.data.SteamGame
import com.example.steamfun.data.formatCount
import kotlinx.coroutines.launch

/**
 * What the screen shows right now.
 *
 * The review count rides along inside [SteamGame] from the first state, but
 * only [Answered] is ever allowed to render it — that is the whole game.
 */
sealed interface RoundState {
    data object Loading : RoundState

    data class Failed(val message: String) : RoundState

    /** A store page is up and the guess is still open. */
    data class Asking(val game: SteamGame, val header: ImageBitmap?) : RoundState

    data class Answered(
        val game: SteamGame,
        val header: ImageBitmap?,
        val correct: Boolean,
        val guessLabel: String,
        val deviationPercent: Int?,
    ) : RoundState
}

class SteamFunViewModel(private val api: SteamApi = SteamApi()) : ViewModel() {

    /** Remaining appids of the current pass; refilled and reshuffled when empty. */
    private val queue = ArrayDeque<Int>()

    var mode by mutableStateOf(GuessMode.ROUNDABOUT)
        private set

    var round by mutableStateOf<RoundState>(RoundState.Loading)
        private set

    var typedGuess by mutableStateOf("")
        private set

    var correctCount by mutableIntStateOf(0)
        private set

    var playedCount by mutableIntStateOf(0)
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
        round = RoundState.Loading
        viewModelScope.launch {
            repeat(MAX_ATTEMPTS) {
                if (queue.isEmpty()) queue.addAll(AppIds.shuffledOrder())
                val game = api.loadGame(queue.removeFirst())
                if (game != null) {
                    // A missing picture costs the artwork, not the round.
                    round = RoundState.Asking(game, api.loadHeader(game.headerImageUrl))
                    return@launch
                }
            }
            round = RoundState.Failed(
                "Keine Steam-Seite erreichbar. Prüf die Internetverbindung und versuch es nochmal.",
            )
        }
    }

    fun submitTypedGuess() {
        val asking = round as? RoundState.Asking ?: return
        val guess = Guessing.parseGuess(typedGuess) ?: return
        val actual = asking.game.totalReviews
        finish(
            asking = asking,
            correct = Guessing.accurateHit(guess, actual),
            guessLabel = formatCount(guess),
            deviation = Guessing.deviationPercent(guess, actual),
        )
    }

    fun submitBucket(bucket: ReviewBucket) {
        val asking = round as? RoundState.Asking ?: return
        finish(
            asking = asking,
            correct = Guessing.roundaboutHit(bucket, asking.game.totalReviews),
            guessLabel = bucket.label,
            deviation = null,
        )
    }

    private fun finish(
        asking: RoundState.Asking,
        correct: Boolean,
        guessLabel: String,
        deviation: Int?,
    ) {
        playedCount++
        if (correct) correctCount++
        round = RoundState.Answered(
            game = asking.game,
            header = asking.header,
            correct = correct,
            guessLabel = guessLabel,
            deviationPercent = deviation,
        )
    }

    private companion object {
        /** Appids that are DLC or unreachable are skipped; this bounds the retrying. */
        const val MAX_ATTEMPTS = 8
        const val MAX_GUESS_DIGITS = 9
    }
}

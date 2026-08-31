package com.example.steamfun.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.steamfun.data.GuessMode
import com.example.steamfun.data.Guessing
import com.example.steamfun.data.ReviewBucket
import com.example.steamfun.data.SteamApi
import com.example.steamfun.data.SteamGame
import com.example.steamfun.data.formatCount
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

class SteamFunViewModel(private val api: SteamApi = SteamApi()) : ViewModel() {

    /**
     * How many titles Steam currently lists, learned from the store itself.
     * Also the number the footer shows, so it is visible that the draw really
     * spans the whole store rather than some shortlist.
     */
    var storeTitleCount by mutableIntStateOf(0)
        private set

    /**
     * Appids from the most recent random slice, shuffled. Refilled from a fresh
     * random offset once used up — one search request then covers many rounds
     * instead of hammering Steam for every single draw.
     */
    private val candidates = ArrayDeque<Int>()

    private var loadJob: Job? = null

    var mode by mutableStateOf(GuessMode.ROUNDABOUT)
        private set

    var round by mutableStateOf<RoundState>(RoundState.Loading("Verbinde mit Steam …"))
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
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repeat(MAX_ATTEMPTS) { attempt ->
                round = RoundState.Loading(
                    if (storeTitleCount == 0) {
                        "Verbinde mit Steam …"
                    } else {
                        "Zufälliges Spiel wird gesucht … (${attempt + 1})"
                    },
                )

                if (candidates.isEmpty() && !drawFreshSlice()) {
                    round = RoundState.Failed(
                        "Steam antwortet gerade nicht. Prüf die Internetverbindung " +
                            "und versuch es nochmal.",
                    )
                    return@launch
                }

                // Some entries are still DLC or region locked; those are skipped.
                val game = api.loadGame(candidates.removeFirst())
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

    /**
     * Jumps to a random offset in Steam's live store listing and takes the
     * appids found there.
     *
     * Any offset is as likely as any other, so the ordering Steam happens to
     * use does not matter — the draw is uniform over everything the store
     * lists. Nothing is kept on the device between runs.
     */
    private suspend fun drawFreshSlice(): Boolean {
        if (storeTitleCount == 0) {
            val probe = api.searchPage(offset = 0, count = 1) ?: return false
            if (probe.totalCount <= 0) return false
            storeTitleCount = probe.totalCount
        }

        val highestOffset = (storeTitleCount - PAGE_SIZE).coerceAtLeast(0)
        val offset = if (highestOffset == 0) 0 else Random.nextInt(highestOffset + 1)

        val page = api.searchPage(offset = offset, count = PAGE_SIZE) ?: return false
        if (page.appIds.isEmpty()) return false
        if (page.totalCount > 0) storeTitleCount = page.totalCount

        candidates.addAll(page.appIds.shuffled())
        return true
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
        /** Entries that turn out not to be games are skipped; this bounds the retrying. */
        const val MAX_ATTEMPTS = 25

        /** Ids taken per search request, so one call feeds several rounds. */
        const val PAGE_SIZE = 50

        const val MAX_GUESS_DIGITS = 9
    }
}

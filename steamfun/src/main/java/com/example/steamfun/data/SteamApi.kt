package com.example.steamfun.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to Steam's public endpoints. None of them needs a key.
 *
 * Every failure — no network, a rate limit, an appid that is not a game —
 * comes back as null so the caller can simply draw another game.
 */
class SteamApi {

    /** Loads a full round, or null if this appid cannot serve one. */
    suspend fun loadGame(appId: Int): SteamGame? = withContext(Dispatchers.IO) {
        val detailsBody = get(
            "https://store.steampowered.com/api/appdetails?appids=$appId&l=english",
        ) ?: return@withContext null
        val page = SteamJson.parseStorePage(appId, detailsBody) ?: return@withContext null

        val reviewsBody = get(
            "https://store.steampowered.com/appreviews/$appId" +
                "?json=1&language=all&purchase_type=all&num_per_page=0",
        ) ?: return@withContext null
        val reviews = SteamJson.parseReviewCount(reviewsBody) ?: return@withContext null

        SteamGame(page = page, totalReviews = reviews)
    }

    /**
     * Asks Steam's live store listing for a slice starting at [offset].
     *
     * Falls back to an unfiltered search once, in case the games category is
     * not honoured — better a page with some DLC on it than no page at all.
     */
    suspend fun searchPage(offset: Int, count: Int): SearchPage? = withContext(Dispatchers.IO) {
        val filtered = get(SteamSearch.url(offset, count, gamesOnly = true))
            ?.let(SteamSearch::parse)
        if (filtered != null && filtered.totalCount > 0) return@withContext filtered

        get(SteamSearch.url(offset, count, gamesOnly = false))?.let(SteamSearch::parse)
    }

    private fun get(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = open(url)
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            // Deliberately no Accept-Encoding: setting it by hand switches off
            // HttpURLConnection's transparent gunzip and hands back raw bytes.
        }

    private companion object {
        const val TIMEOUT_MS = 15_000

        /** Steam serves the plain endpoints more reliably with a real user agent. */
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile"
    }
}

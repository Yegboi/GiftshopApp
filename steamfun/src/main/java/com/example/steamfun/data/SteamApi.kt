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
     * Downloads Steam's whole catalogue of appids. Tens of megabytes of JSON,
     * so it is streamed and only the numbers are kept — see [AppListParser].
     */
    suspend fun downloadAppIds(): IntArray? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = open("https://api.steampowered.com/ISteamApps/GetAppList/v2/")
            if (connection.responseCode !in 200..299) return@withContext null
            val ids = connection.inputStream.use { AppListParser.readAppIds(it) }
            ids.takeIf { it.size >= MIN_PLAUSIBLE_CATALOGUE }
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
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
            readTimeout = CATALOGUE_READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            // Deliberately no Accept-Encoding: setting it by hand switches off
            // HttpURLConnection's transparent gunzip and hands back raw bytes.
        }

    private companion object {
        const val TIMEOUT_MS = 15_000

        /** The catalogue is large; give it room before giving up. */
        const val CATALOGUE_READ_TIMEOUT_MS = 60_000

        /** A far smaller answer than this means something went wrong upstream. */
        const val MIN_PLAUSIBLE_CATALOGUE = 10_000

        /** Steam serves the plain endpoints more reliably with a real user agent. */
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile"
    }
}

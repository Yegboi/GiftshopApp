package com.example.steamfun.data

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to Steam's two public store endpoints. Neither needs a key.
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
        val details = SteamJson.parseGameDetails(appId, detailsBody) ?: return@withContext null

        val reviewsBody = get(
            "https://store.steampowered.com/appreviews/$appId" +
                "?json=1&language=all&purchase_type=all&num_per_page=0",
        ) ?: return@withContext null
        val reviews = SteamJson.parseReviewCount(reviewsBody) ?: return@withContext null

        SteamGame(
            appId = appId,
            name = details.name,
            headerImageUrl = details.headerImageUrl,
            totalReviews = reviews,
        )
    }

    /** Header artwork. A missing image only costs the picture, not the round. */
    suspend fun loadHeader(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = open(url)
            if (connection.responseCode !in 200..299) return@withContext null
            connection.inputStream.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
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
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
        }

    private companion object {
        const val TIMEOUT_MS = 12_000

        /** Steam serves the plain endpoints more reliably with a real user agent. */
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile"
    }
}

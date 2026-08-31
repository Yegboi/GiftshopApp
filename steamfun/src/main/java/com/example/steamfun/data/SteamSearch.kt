package com.example.steamfun.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** One slice of Steam's store listing: how many titles exist, and the ids on this page. */
data class SearchPage(val totalCount: Int, val appIds: List<Int>)

/**
 * Draws games straight from Steam's live store listing.
 *
 * The store search answers with `total_count` — every title Steam currently
 * lists — and a slice of results starting at any offset. Jumping to a random
 * offset therefore picks uniformly from the whole database, no matter how the
 * results happen to be ordered, and needs nothing stored on the device.
 *
 * That is the point: no downloaded index, no bundled list, nothing that could
 * quietly narrow the pool back down to games people have heard of.
 */
object SteamSearch {

    private val json = Json { ignoreUnknownKeys = true }

    /** Appids sit on the anchor of every result row. Bundles list several. */
    private val APP_ID = Regex("data-ds-appid=\"([0-9,]+)\"")

    /** Steam's own category id for games, so DLC and soundtracks stay out. */
    const val GAMES_CATEGORY = 998

    fun url(offset: Int, count: Int, gamesOnly: Boolean = true): String = buildString {
        append("https://store.steampowered.com/search/results/?json=1&infinite=1")
        append("&ignore_preferences=1")
        if (gamesOnly) append("&category1=$GAMES_CATEGORY")
        append("&start=").append(offset.coerceAtLeast(0))
        append("&count=").append(count.coerceIn(1, 100))
    }

    fun parse(body: String): SearchPage? {
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
            ?: return null

        val total = (root["total_count"] as? JsonPrimitive)?.let { field ->
            field.intOrNull ?: field.contentOrNull?.toIntOrNull()
        } ?: return null
        if (total < 0) return null

        val html = (root["results_html"] as? JsonPrimitive)?.contentOrNull.orEmpty()

        val ids = APP_ID.findAll(html)
            .mapNotNull { match ->
                // A bundle row carries several ids; the first is the headline app.
                match.groupValues[1].substringBefore(',').toIntOrNull()
            }
            .filter { it > 0 }
            .distinct()
            .toList()

        return SearchPage(totalCount = total, appIds = ids)
    }
}

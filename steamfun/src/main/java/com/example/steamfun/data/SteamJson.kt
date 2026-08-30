package com.example.steamfun.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** Name and artwork of a store page, as far as the game needs them. */
data class GameDetails(val name: String, val headerImageUrl: String)

/**
 * Reads Steam's two public endpoints.
 *
 * Navigated element by element rather than mapped onto data classes: Steam
 * sends `"data": []` instead of an object when it has nothing for an appid,
 * which would blow up a typed decoder. Anything unexpected yields null and the
 * caller draws another game.
 */
object SteamJson {

    private val json = Json { ignoreUnknownKeys = true }

    /** Artwork URL derivable from the appid alone, if the payload omits it. */
    fun headerImageUrl(appId: Int): String =
        "https://cdn.cloudflare.steamstatic.com/steam/apps/$appId/header.jpg"

    fun storeUrl(appId: Int): String = "https://store.steampowered.com/app/$appId/"

    /**
     * From `store.steampowered.com/api/appdetails?appids=<id>`. Returns null for
     * anything that is not a released game — DLC, soundtracks and tools would
     * make dull rounds.
     */
    fun parseGameDetails(appId: Int, body: String): GameDetails? {
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
            ?: return null
        val entry = root[appId.toString()] as? JsonObject ?: return null
        if ((entry["success"] as? JsonPrimitive)?.booleanOrNull != true) return null

        val data = entry["data"] as? JsonObject ?: return null
        if ((data["type"] as? JsonPrimitive)?.contentOrNull != "game") return null

        val name = (data["name"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: return null
        val image = (data["header_image"] as? JsonPrimitive)?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: headerImageUrl(appId)

        return GameDetails(name = name, headerImageUrl = image)
    }

    /**
     * From `store.steampowered.com/appreviews/<id>?json=1`. This is the count
     * the game is played for, so a missing or malformed one must not be guessed
     * at — it returns null and the round is redrawn.
     */
    fun parseReviewCount(body: String): Int? {
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
            ?: return null
        if ((root["success"] as? JsonPrimitive)?.intOrNull != 1) return null
        val summary = root["query_summary"] as? JsonObject ?: return null
        return (summary["total_reviews"] as? JsonPrimitive)?.intOrNull?.takeIf { it >= 0 }
    }
}

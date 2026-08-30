package com.example.steamfun.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

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

    fun headerImageUrl(appId: Int): String =
        "https://cdn.cloudflare.steamstatic.com/steam/apps/$appId/header.jpg"

    fun storeUrl(appId: Int): String = "https://store.steampowered.com/app/$appId/"

    /**
     * Steam still hands out plain `http://` media links. Android blocks
     * cleartext by default, and the CDN serves the same paths over TLS, so
     * every URL is lifted before it reaches a player or an image loader.
     */
    fun secure(url: String): String =
        if (url.startsWith("http://")) "https://" + url.removePrefix("http://") else url

    /**
     * From `store.steampowered.com/api/appdetails?appids=<id>`. Returns null for
     * anything that is not a game — DLC, soundtracks and tools would make dull
     * rounds.
     */
    fun parseStorePage(appId: Int, body: String): StorePage? {
        val root = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
            ?: return null
        val entry = root[appId.toString()] as? JsonObject ?: return null
        if ((entry["success"] as? JsonPrimitive)?.booleanOrNull != true) return null

        val data = entry["data"] as? JsonObject ?: return null
        if (data.string("type") != "game") return null
        val name = data.string("name")?.takeIf { it.isNotBlank() } ?: return null

        return StorePage(
            appId = appId,
            name = name,
            headerImageUrl = data.string("header_image")?.takeIf { it.isNotBlank() }
                ?.let(::secure)
                ?: headerImageUrl(appId),
            shortDescription = data.string("short_description").orEmpty(),
            detailedDescriptionHtml = data.string("detailed_description")
                ?: data.string("about_the_game").orEmpty(),
            developers = data.stringList("developers"),
            publishers = data.stringList("publishers"),
            genres = (data["genres"] as? JsonArray).orEmpty()
                .mapNotNull { (it as? JsonObject)?.string("description") },
            releaseDate = (data["release_date"] as? JsonObject)?.string("date").orEmpty(),
            price = readPrice(data),
            screenshots = (data["screenshots"] as? JsonArray).orEmpty()
                .mapNotNull { shot -> (shot as? JsonObject)?.let(::readScreenshot) },
            trailers = (data["movies"] as? JsonArray).orEmpty()
                .mapNotNull { movie -> (movie as? JsonObject)?.let(::readTrailer) },
        )
    }

    /**
     * From `store.steampowered.com/appreviews/<id>?json=1`. This is the number
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

    private fun readPrice(data: JsonObject): String {
        if ((data["is_free"] as? JsonPrimitive)?.booleanOrNull == true) return "Kostenlos"
        val overview = data["price_overview"] as? JsonObject ?: return ""
        return overview.string("final_formatted").orEmpty()
    }

    private fun readScreenshot(shot: JsonObject): Screenshot? {
        val full = shot.string("path_full")?.takeIf { it.isNotBlank() }
        val thumb = shot.string("path_thumbnail")?.takeIf { it.isNotBlank() }
        val chosen = full ?: thumb ?: return null
        return Screenshot(
            thumbnailUrl = secure(thumb ?: chosen),
            fullUrl = secure(chosen),
        )
    }

    /** Prefers mp4 over webm and the larger cut, since a trailer is worth the bytes. */
    private fun readTrailer(movie: JsonObject): Trailer? {
        val video = listOf("mp4", "webm").firstNotNullOfOrNull { container ->
            val streams = movie[container] as? JsonObject ?: return@firstNotNullOfOrNull null
            listOf("max", "480").firstNotNullOfOrNull { quality ->
                streams.string(quality)?.takeIf { it.isNotBlank() }
            }
        } ?: return null

        return Trailer(
            name = movie.string("name")?.takeIf { it.isNotBlank() } ?: "Trailer",
            thumbnailUrl = movie.string("thumbnail")?.takeIf { it.isNotBlank() }
                ?.let(::secure)
                .orEmpty(),
            videoUrl = secure(video),
        )
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.stringList(key: String): List<String> =
        (this[key] as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .filter { it.isNotBlank() }

    private fun JsonArray?.orEmpty(): List<kotlinx.serialization.json.JsonElement> =
        this ?: emptyList()
}

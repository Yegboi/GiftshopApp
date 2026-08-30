package com.example.steamfun.data

/** One picture from the store page, in list and full size. */
data class Screenshot(val thumbnailUrl: String, val fullUrl: String)

/** A trailer, with the poster frame and the stream to play. */
data class Trailer(val name: String, val thumbnailUrl: String, val videoUrl: String)

/**
 * A Steam store page as far as the game shows it — everything except the one
 * number being guessed.
 */
data class StorePage(
    val appId: Int,
    val name: String,
    val headerImageUrl: String,
    val shortDescription: String,
    val detailedDescriptionHtml: String,
    val developers: List<String>,
    val publishers: List<String>,
    val genres: List<String>,
    val releaseDate: String,
    val price: String,
    val screenshots: List<Screenshot>,
    val trailers: List<Trailer>,
) {
    val storeUrl: String get() = SteamJson.storeUrl(appId)
}

/** A store page plus the answer. Only the result screen may read [totalReviews]. */
data class SteamGame(val page: StorePage, val totalReviews: Int) {
    val name: String get() = page.name
}

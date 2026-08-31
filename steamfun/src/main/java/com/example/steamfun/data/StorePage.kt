package com.example.steamfun.data

import java.time.Year

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
    val comingSoon: Boolean,
    val price: String,
    val screenshots: List<Screenshot>,
    val trailers: List<Trailer>,
) {
    val storeUrl: String get() = SteamJson.storeUrl(appId)

    /** Unreleased games have no reviews, which makes them a pointless round. */
    val isReleased: Boolean
        get() = ReleaseFilter.isReleased(comingSoon, releaseDate, Year.now().value)
}

/**
 * Decides whether a store page is for a game that is actually out.
 *
 * Steam's own `coming_soon` flag settles most of it, but not all: some pages
 * carry a plain future date instead. The date field is free text — "10 Oct,
 * 2007", "Q4 2027", "2027", "To be announced" — so the year is picked out of
 * it and anything later than now, or with no year at all, counts as unreleased.
 * Being strict only costs a redraw; being lax puts a game with no reviews on
 * screen, which is not worth guessing about.
 */
object ReleaseFilter {

    private val YEAR = Regex("(19|20)\\d{2}")

    fun isReleased(comingSoon: Boolean, releaseDate: String, currentYear: Int): Boolean {
        if (comingSoon) return false
        val year = YEAR.find(releaseDate)?.value?.toIntOrNull() ?: return false
        return year <= currentYear
    }
}

/** A store page plus the answer. Only the result screen may read [totalReviews]. */
data class SteamGame(val page: StorePage, val totalReviews: Int) {
    val name: String get() = page.name
}

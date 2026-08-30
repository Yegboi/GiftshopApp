package com.example.steamfun.data

/** One round's store page, exactly as Steam described it a moment ago. */
data class SteamGame(
    val appId: Int,
    val name: String,
    val headerImageUrl: String,
    val totalReviews: Int,
) {
    val storeUrl: String get() = SteamJson.storeUrl(appId)
}

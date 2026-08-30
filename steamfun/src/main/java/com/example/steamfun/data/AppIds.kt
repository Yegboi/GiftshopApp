package com.example.steamfun.data

/**
 * Fallback list of Steam appids.
 *
 * Rounds normally draw from Steam's whole catalogue, downloaded on first
 * launch. This list only stands in when that download fails, so the game still
 * runs offline of the catalogue rather than refusing to start.
 *
 * Only ids live here — name, artwork and review count are fetched per round, so
 * nothing about a game is baked in and the count shown is always current.
 */
object AppIds {

    val candidates: List<Int> = listOf(
        // Valve
        10, 70, 220, 240, 400, 440, 500, 550, 570, 620, 730, 546560,
        // Long-running multiplayer
        4000, 105600, 108600, 230410, 236390, 244850, 252490, 291550,
        304930, 322330, 359550, 444090, 578080, 594650, 739630, 892970,
        945360, 1085660, 1172470, 1203220, 1966720,
        // Single-player and story games
        22380, 49520, 72850, 271590, 275850, 292030, 377160, 379720,
        489830, 524220, 552520, 582010, 601150, 782330, 883710, 1091500,
        1151640, 1174180, 1245620, 1332010, 1817070, 1888930, 2050650,
        // Strategy and simulation
        8930, 236850, 268500, 281990, 289070, 294100, 394360, 427520,
        813780, 1466860,
        // Indie
        250900, 268910, 322170, 367520, 387290, 413150, 504230, 588650,
        620980, 632360, 646570, 1057090, 1145360, 1794680,
        // Survival and co-op
        242760, 261550, 1326470, 218620, 1272080,
        // Card and puzzle
        1449850, 1687950, 1113000,
    ).distinct()

    /** Draws candidates in random order, never repeating within one pass. */
    fun shuffledOrder(seed: Long? = null): List<Int> =
        if (seed == null) candidates.shuffled() else candidates.shuffled(java.util.Random(seed))
}

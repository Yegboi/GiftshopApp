package com.example.steamfun.data

import java.io.InputStream

/**
 * Pulls appids out of Steam's full catalogue.
 *
 * `ISteamApps/GetAppList/v2` answers with every app on Steam — hundreds of
 * thousands of entries, tens of megabytes. Decoding that into objects would be
 * wasteful and might not fit in memory, so the stream is scanned for the
 * literal `"appid":` and only the numbers are kept. A few hundred thousand ints
 * are about a megabyte.
 */
object AppListParser {

    private const val KEY = "\"appid\""

    /** Reads ids from [input] until it ends or [limit] ids have been collected. */
    fun readAppIds(input: InputStream, limit: Int = 500_000): IntArray {
        val ids = ArrayList<Int>(minOf(limit, 300_000))
        val reader = input.bufferedReader()

        var matched = 0          // how much of KEY has been seen
        var inNumber = false
        var digitsSeen = false
        var value = 0L
        var awaitingColon = false

        while (true) {
            val read = reader.read()
            if (read < 0) break
            val c = read.toChar()

            if (inNumber) {
                if (c in '0'..'9') {
                    digitsSeen = true
                    value = value * 10 + (c - '0')
                    // Ignore anything absurd rather than overflowing.
                    if (value > Int.MAX_VALUE) value = Int.MAX_VALUE.toLong()
                    continue
                }
                // Whitespace between the colon and the first digit is still padding.
                if (!digitsSeen && c.isWhitespace()) continue
                if (value > 0) {
                    ids.add(value.toInt())
                    if (ids.size >= limit) break
                }
                inNumber = false
                digitsSeen = false
                value = 0
                // fall through so this character can start a new match
            }

            if (awaitingColon) {
                when {
                    c.isWhitespace() -> continue
                    c == ':' -> {
                        awaitingColon = false
                        inNumber = true
                        digitsSeen = false
                        value = 0
                        continue
                    }
                    else -> awaitingColon = false
                }
            }

            matched = if (c == KEY[matched]) matched + 1 else if (c == KEY[0]) 1 else 0
            if (matched == KEY.length) {
                matched = 0
                awaitingColon = true
            }
        }

        if (inNumber && value > 0 && ids.size < limit) ids.add(value.toInt())
        return ids.toIntArray()
    }
}

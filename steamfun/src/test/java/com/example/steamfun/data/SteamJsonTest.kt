package com.example.steamfun.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SteamJsonTest {

    // ---- appdetails ----

    private fun details(appId: Int, type: String = "game", name: String = "Team Fortress 2") = """
        {"$appId":{"success":true,"data":{
          "type":"$type","name":"$name","steam_appid":$appId,
          "header_image":"https://cdn.example/steam/apps/$appId/header.jpg",
          "is_free":true,"short_description":"egal"
        }}}
    """.trimIndent()

    @Test
    fun `reads name and artwork of a game`() {
        val parsed = SteamJson.parseGameDetails(440, details(440))

        assertNotNull(parsed)
        assertEquals("Team Fortress 2", parsed!!.name)
        assertEquals("https://cdn.example/steam/apps/440/header.jpg", parsed.headerImageUrl)
    }

    @Test
    fun `skips anything that is not a game`() {
        listOf("dlc", "music", "demo", "video", "hardware").forEach { type ->
            assertNull("$type wurde nicht gefiltert", SteamJson.parseGameDetails(1, details(1, type)))
        }
    }

    @Test
    fun `an unknown appid sends data as an empty array instead of an object`() {
        // This is Steam's actual shape for a missing entry and would break a
        // typed decoder, so it has to come back as null rather than throw.
        val body = """{"9999999":{"success":false,"data":[]}}"""

        assertNull(SteamJson.parseGameDetails(9_999_999, body))
    }

    @Test
    fun `an unsuccessful entry is rejected`() {
        assertNull(SteamJson.parseGameDetails(1, """{"1":{"success":false}}"""))
    }

    @Test
    fun `an entry for a different appid is not accepted`() {
        assertNull(SteamJson.parseGameDetails(999, details(440)))
    }

    @Test
    fun `a blank name is rejected rather than shown empty`() {
        assertNull(SteamJson.parseGameDetails(440, details(440, name = "   ")))
    }

    @Test
    fun `a missing artwork falls back to the url built from the appid`() {
        val body = """{"440":{"success":true,"data":{"type":"game","name":"TF2"}}}"""

        val parsed = SteamJson.parseGameDetails(440, body)

        assertEquals(SteamJson.headerImageUrl(440), parsed!!.headerImageUrl)
    }

    @Test
    fun `garbage does not throw`() {
        listOf("", "   ", "not json", "[]", "{", "null", """{"440":[]}""").forEach {
            assertNull("bei <$it>", SteamJson.parseGameDetails(440, it))
        }
    }

    // ---- appreviews ----

    private fun reviews(total: Int) = """
        {"success":1,"query_summary":{
          "num_reviews":0,"review_score":8,"review_score_desc":"Very Positive",
          "total_positive":${total - 1},"total_negative":1,"total_reviews":$total
        },"reviews":[],"cursor":"*"}
    """.trimIndent()

    @Test
    fun `reads the total review count`() {
        assertEquals(143_271, SteamJson.parseReviewCount(reviews(143_271)))
    }

    @Test
    fun `a game without reviews reads as zero, not as missing`() {
        val body = """{"success":1,"query_summary":{"total_reviews":0}}"""

        assertEquals(0, SteamJson.parseReviewCount(body))
    }

    @Test
    fun `an unsuccessful response is rejected`() {
        assertNull(SteamJson.parseReviewCount("""{"success":2,"query_summary":{"total_reviews":5}}"""))
    }

    @Test
    fun `a missing count is not guessed at`() {
        assertNull(SteamJson.parseReviewCount("""{"success":1,"query_summary":{}}"""))
        assertNull(SteamJson.parseReviewCount("""{"success":1}"""))
    }

    @Test
    fun `review garbage does not throw`() {
        listOf("", "kein json", "[]", "{", """{"success":1,"query_summary":[]}""").forEach {
            assertNull("bei <$it>", SteamJson.parseReviewCount(it))
        }
    }

    // ---- urls ----

    @Test
    fun `the store url points at the app page`() {
        assertEquals("https://store.steampowered.com/app/440/", SteamJson.storeUrl(440))
    }

    @Test
    fun `the artwork url is the steam cdn path`() {
        assertTrue(SteamJson.headerImageUrl(440).endsWith("/steam/apps/440/header.jpg"))
    }
}

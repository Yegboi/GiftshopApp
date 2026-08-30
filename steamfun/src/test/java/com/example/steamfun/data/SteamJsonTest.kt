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
        val parsed = SteamJson.parseStorePage(440, details(440))

        assertNotNull(parsed)
        assertEquals("Team Fortress 2", parsed!!.name)
        assertEquals("https://cdn.example/steam/apps/440/header.jpg", parsed.headerImageUrl)
    }

    @Test
    fun `skips anything that is not a game`() {
        listOf("dlc", "music", "demo", "video", "hardware").forEach { type ->
            assertNull("$type wurde nicht gefiltert", SteamJson.parseStorePage(1, details(1, type)))
        }
    }

    @Test
    fun `an unknown appid sends data as an empty array instead of an object`() {
        // This is Steam's actual shape for a missing entry and would break a
        // typed decoder, so it has to come back as null rather than throw.
        val body = """{"9999999":{"success":false,"data":[]}}"""

        assertNull(SteamJson.parseStorePage(9_999_999, body))
    }

    @Test
    fun `an unsuccessful entry is rejected`() {
        assertNull(SteamJson.parseStorePage(1, """{"1":{"success":false}}"""))
    }

    @Test
    fun `an entry for a different appid is not accepted`() {
        assertNull(SteamJson.parseStorePage(999, details(440)))
    }

    @Test
    fun `a blank name is rejected rather than shown empty`() {
        assertNull(SteamJson.parseStorePage(440, details(440, name = "   ")))
    }

    @Test
    fun `a missing artwork falls back to the url built from the appid`() {
        val body = """{"440":{"success":true,"data":{"type":"game","name":"TF2"}}}"""

        val parsed = SteamJson.parseStorePage(440, body)

        assertEquals(SteamJson.headerImageUrl(440), parsed!!.headerImageUrl)
    }

    @Test
    fun `garbage does not throw`() {
        listOf("", "   ", "not json", "[]", "{", "null", """{"440":[]}""").forEach {
            assertNull("bei <$it>", SteamJson.parseStorePage(440, it))
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

class StorePageParsingTest {

    private val body = """
        {"440":{"success":true,"data":{
          "type":"game","name":"Team Fortress 2","steam_appid":440,
          "is_free":true,
          "short_description":"Kurz gesagt: Hüte.",
          "detailed_description":"<h2>Über</h2><p>Text</p><img src=\"https://cdn/x.gif\">",
          "developers":["Valve"],"publishers":["Valve",""],
          "header_image":"http://cdn.example/header.jpg",
          "genres":[{"id":"1","description":"Action"},{"id":"2","description":"Free to Play"}],
          "release_date":{"coming_soon":false,"date":"10 Oct, 2007"},
          "screenshots":[
            {"id":0,"path_thumbnail":"http://cdn/t0.jpg","path_full":"http://cdn/f0.jpg"},
            {"id":1,"path_thumbnail":"https://cdn/t1.jpg","path_full":"https://cdn/f1.jpg"}
          ],
          "movies":[{"id":1,"name":"Meet the Heavy","thumbnail":"http://cdn/mt.jpg",
                     "webm":{"480":"http://cdn/w480.webm","max":"http://cdn/wmax.webm"},
                     "mp4":{"480":"http://cdn/m480.mp4","max":"http://cdn/mmax.mp4"},
                     "highlight":true}]
        }}}
    """.trimIndent()

    private val page = SteamJson.parseStorePage(440, body)!!

    @Test
    fun `reads the descriptions`() {
        assertEquals("Kurz gesagt: Hüte.", page.shortDescription)
        assertTrue(page.detailedDescriptionHtml.contains("<img"))
    }

    @Test
    fun `reads credits genres and release date`() {
        assertEquals(listOf("Valve"), page.developers)
        assertEquals(listOf("Valve"), page.publishers) // the blank entry is dropped
        assertEquals(listOf("Action", "Free to Play"), page.genres)
        assertEquals("10 Oct, 2007", page.releaseDate)
    }

    @Test
    fun `a free game says so instead of showing an empty price`() {
        assertEquals("Kostenlos", page.price)
    }

    @Test
    fun `reads a formatted price when the game is not free`() {
        val paid = body.replace("\"is_free\":true", "\"is_free\":false") +
            ""
        val withPrice = paid.replace(
            "\"short_description\"",
            "\"price_overview\":{\"final_formatted\":\"19,99€\"},\"short_description\"",
        )

        assertEquals("19,99€", SteamJson.parseStorePage(440, withPrice)!!.price)
    }

    @Test
    fun `reads every screenshot`() {
        assertEquals(2, page.screenshots.size)
        assertEquals("https://cdn/f0.jpg", page.screenshots[0].fullUrl)
        assertEquals("https://cdn/t0.jpg", page.screenshots[0].thumbnailUrl)
    }

    @Test
    fun `prefers the large mp4 for a trailer`() {
        assertEquals(1, page.trailers.size)
        assertEquals("Meet the Heavy", page.trailers[0].name)
        assertEquals("https://cdn/mmax.mp4", page.trailers[0].videoUrl)
    }

    @Test
    fun `falls back to webm when there is no mp4`() {
        val webmOnly = body.replace(
            """"mp4":{"480":"http://cdn/m480.mp4","max":"http://cdn/mmax.mp4"},""", "",
        )

        val trailer = SteamJson.parseStorePage(440, webmOnly)!!.trailers.single()

        assertEquals("https://cdn/wmax.webm", trailer.videoUrl)
    }

    @Test
    fun `falls back to the smaller cut when max is missing`() {
        val small = body.replace(""""max":"http://cdn/mmax.mp4"""", """"other":"x"""")

        assertEquals("https://cdn/m480.mp4", SteamJson.parseStorePage(440, small)!!.trailers[0].videoUrl)
    }

    @Test
    fun `every media url is lifted to https`() {
        // Android blocks cleartext, and Steam still hands out http links.
        val urls = page.screenshots.flatMap { listOf(it.fullUrl, it.thumbnailUrl) } +
            page.trailers.flatMap { listOf(it.videoUrl, it.thumbnailUrl) } +
            page.headerImageUrl

        urls.forEach { assertTrue("$it ist nicht https", it.startsWith("https://")) }
    }

    @Test
    fun `a page without media still parses`() {
        val bare = """{"440":{"success":true,"data":{"type":"game","name":"Nackt"}}}"""

        val parsed = SteamJson.parseStorePage(440, bare)!!

        assertEquals("Nackt", parsed.name)
        assertTrue(parsed.screenshots.isEmpty())
        assertTrue(parsed.trailers.isEmpty())
        assertEquals("", parsed.shortDescription)
    }

    @Test
    fun `a movie with no playable stream is skipped`() {
        val broken = body.replace(
            """"webm":{"480":"http://cdn/w480.webm","max":"http://cdn/wmax.webm"},""", "",
        ).replace(
            """"mp4":{"480":"http://cdn/m480.mp4","max":"http://cdn/mmax.mp4"},""", "",
        )

        assertTrue(SteamJson.parseStorePage(440, broken)!!.trailers.isEmpty())
    }

    @Test
    fun `about_the_game stands in when detailed_description is absent`() {
        val fallback = """
            {"440":{"success":true,"data":{"type":"game","name":"X",
             "about_the_game":"<p>Ersatz</p>"}}}
        """.trimIndent()

        assertEquals("<p>Ersatz</p>", SteamJson.parseStorePage(440, fallback)!!.detailedDescriptionHtml)
    }

    @Test
    fun `secure leaves https and relative urls alone`() {
        assertEquals("https://a/b", SteamJson.secure("https://a/b"))
        assertEquals("//a/b", SteamJson.secure("//a/b"))
        assertEquals("", SteamJson.secure(""))
    }
}

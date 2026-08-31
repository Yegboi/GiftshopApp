package com.example.steamfun.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SteamSearchTest {

    private fun response(total: Any, html: String): String {
        val escaped = html.replace("\\", "\\\\").replace("\"", "\\\"")
        val totalField = if (total is String) "\"$total\"" else "$total"
        return """{"desc":"","total_count":$totalField,"start":0,"results_html":"$escaped"}"""
    }

    private val row = """<a href="https://store.steampowered.com/app/440/" data-ds-appid="440">TF2</a>"""

    @Test
    fun `reads the total and the ids on the page`() {
        val page = SteamSearch.parse(response(91_234, row))!!

        assertEquals(91_234, page.totalCount)
        assertEquals(listOf(440), page.appIds)
    }

    @Test
    fun `reads a total that arrives as a string`() {
        assertEquals(500, SteamSearch.parse(response("500", row))!!.totalCount)
    }

    @Test
    fun `reads every row of a page`() {
        val html = (1..25).joinToString("") { """<a data-ds-appid="$it">Spiel $it</a>""" }

        assertEquals((1..25).toList(), SteamSearch.parse(response(1000, html))!!.appIds)
    }

    @Test
    fun `a bundle row contributes its headline app`() {
        val html = """<a data-ds-appid="440,570,730">Bundle</a>"""

        assertEquals(listOf(440), SteamSearch.parse(response(10, html))!!.appIds)
    }

    @Test
    fun `the same id twice counts once`() {
        val html = """<a data-ds-appid="440"></a><a data-ds-appid="440"></a>"""

        assertEquals(listOf(440), SteamSearch.parse(response(10, html))!!.appIds)
    }

    @Test
    fun `a page past the end has a total but no rows`() {
        val page = SteamSearch.parse(response(1000, ""))!!

        assertEquals(1000, page.totalCount)
        assertTrue(page.appIds.isEmpty())
    }

    @Test
    fun `a response without a total is unusable`() {
        assertNull(SteamSearch.parse("""{"results_html":"$row"}"""))
        assertNull(SteamSearch.parse("{}"))
    }

    @Test
    fun `garbage does not throw`() {
        listOf("", "kein json", "[]", "{", "null").forEach {
            assertNull("bei <$it>", SteamSearch.parse(it))
        }
    }

    @Test
    fun `the url asks steam for games only by default`() {
        val url = SteamSearch.url(offset = 1_234, count = 50)

        assertTrue(url.contains("start=1234"))
        assertTrue(url.contains("count=50"))
        assertTrue(url.contains("category1=${SteamSearch.GAMES_CATEGORY}"))
        assertTrue(url.contains("json=1"))
    }

    @Test
    fun `the games filter can be dropped`() {
        assertTrue(!SteamSearch.url(0, 10, gamesOnly = false).contains("category1"))
    }

    @Test
    fun `nonsense paging values are brought into range`() {
        assertTrue(SteamSearch.url(offset = -5, count = 0).contains("start=0"))
        assertTrue(SteamSearch.url(offset = 0, count = 0).contains("count=1"))
        assertTrue(SteamSearch.url(offset = 0, count = 5_000).contains("count=100"))
    }
}

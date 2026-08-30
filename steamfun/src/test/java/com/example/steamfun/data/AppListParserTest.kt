package com.example.steamfun.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListParserTest {

    private fun parse(body: String, limit: Int = 500_000) =
        AppListParser.readAppIds(body.byteInputStream(), limit)

    @Test
    fun `reads the ids from the catalogue shape steam returns`() {
        val body = """
            {"applist":{"apps":[
              {"appid":10,"name":"Counter-Strike"},
              {"appid":20,"name":"Team Fortress Classic"},
              {"appid":2028850,"name":"Irgendein Indie"}
            ]}}
        """.trimIndent()

        assertArrayEquals(intArrayOf(10, 20, 2_028_850), parse(body))
    }

    @Test
    fun `tolerates whitespace around the colon`() {
        assertArrayEquals(intArrayOf(7), parse("""{"appid"   :   7}"""))
    }

    @Test
    fun `an empty catalogue yields nothing`() {
        assertEquals(0, parse("""{"applist":{"apps":[]}}""").size)
        assertEquals(0, parse("").size)
    }

    @Test
    fun `names that mention appid do not become ids`() {
        // A game called "appid" would otherwise inject a bogus number.
        val body = """{"applist":{"apps":[{"appid":5,"name":"the appid game"}]}}"""

        assertArrayEquals(intArrayOf(5), parse(body))
    }

    @Test
    fun `the last id is kept even without a trailing delimiter`() {
        assertArrayEquals(intArrayOf(42), parse("""{"appid":42"""))
    }

    @Test
    fun `the limit stops the scan early`() {
        val body = (1..100).joinToString(",", "[", "]") { """{"appid":$it}""" }

        assertEquals(10, parse(body, limit = 10).size)
    }

    @Test
    fun `handles a catalogue far bigger than one buffer`() {
        val body = (1..50_000).joinToString(",", """{"applist":{"apps":[""", "]}}") {
            """{"appid":$it,"name":"Spiel $it"}"""
        }

        val ids = parse(body)

        assertEquals(50_000, ids.size)
        assertEquals(1, ids.first())
        assertEquals(50_000, ids.last())
    }

    @Test
    fun `zero is not a usable appid`() {
        assertArrayEquals(intArrayOf(3), parse("""[{"appid":0},{"appid":3}]"""))
    }

    @Test
    fun `every id read back is positive`() {
        val ids = parse("""[{"appid":1},{"appid":999999},{"appid":2500000}]""")
        assertTrue(ids.all { it > 0 })
    }
}

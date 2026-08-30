package com.example.steamfun.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AppIdCacheTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val week = 7L * 24 * 60 * 60 * 1000

    @Test
    fun `nothing cached yet reads as null`() {
        assertNull(AppIdCache(File(folder.root, "ids.bin")).load(week))
    }

    @Test
    fun `ids round-trip`() {
        val cache = AppIdCache(File(folder.root, "ids.bin"))
        val ids = intArrayOf(10, 20, 730, 2_028_850)

        assertTrue(cache.save(ids))
        assertArrayEquals(ids, cache.load(week))
    }

    @Test
    fun `a large catalogue round-trips`() {
        val cache = AppIdCache(File(folder.root, "big.bin"))
        val ids = IntArray(250_000) { it + 1 }

        assertTrue(cache.save(ids))
        assertArrayEquals(ids, cache.load(week))
    }

    @Test
    fun `a stale cache is ignored so the catalogue gets refreshed`() {
        val file = File(folder.root, "old.bin")
        val cache = AppIdCache(file)
        cache.save(intArrayOf(1, 2, 3))
        file.setLastModified(System.currentTimeMillis() - 2 * week)

        assertNull(cache.load(week))
    }

    @Test
    fun `a truncated file does not crash the app`() {
        val file = File(folder.root, "cut.bin")
        AppIdCache(file).save(IntArray(1_000) { it + 1 })
        file.writeBytes(file.readBytes().copyOf(40))

        assertNull(AppIdCache(file).load(week))
    }

    @Test
    fun `a foreign file is rejected by the magic number`() {
        val file = File(folder.root, "foreign.bin")
        file.writeBytes(ByteArray(64) { 7 })

        assertNull(AppIdCache(file).load(week))
    }

    @Test
    fun `save creates missing directories`() {
        val file = File(folder.root, "nested/deep/ids.bin")

        assertTrue(AppIdCache(file).save(intArrayOf(5)))
        assertArrayEquals(intArrayOf(5), AppIdCache(file).load(week))
    }
}

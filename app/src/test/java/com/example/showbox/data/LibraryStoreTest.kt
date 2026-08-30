package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LibraryStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun storeIn(name: String = "library.json"): Pair<LibraryStore, File> {
        val file = File(folder.root, name)
        return LibraryStore(file) to file
    }

    @Test
    fun `load returns null when nothing was saved yet`() {
        val (store, _) = storeIn()
        assertNull(store.load())
    }

    @Test
    fun `saved data comes back unchanged`() {
        val (store, _) = storeIn()
        val data = LibraryData(
            entries = listOf(
                Entry("a", Category.QUIZ, "Frage?", "Antwort"),
                Entry("b", Category.SPEED_DATING, "Prompt"),
            ),
            songs = listOf(Song("content://audio/1", "Ein Lied")),
        )

        assertTrue(store.save(data))

        assertEquals(data, store.load())
    }

    @Test
    fun `an empty library round-trips`() {
        val (store, _) = storeIn()
        assertTrue(store.save(LibraryData()))
        assertEquals(LibraryData(), store.load())
    }

    @Test
    fun `saving twice keeps only the newer data`() {
        val (store, _) = storeIn()
        store.save(LibraryData(entries = listOf(Entry("a", Category.QUIZ, "Alt", "x"))))
        store.save(LibraryData(entries = listOf(Entry("b", Category.QUIZ, "Neu", "y"))))

        val loaded = store.load()
        assertNotNull(loaded)
        assertEquals(listOf("Neu"), loaded!!.entries.map { it.prompt })
    }

    @Test
    fun `a corrupt file is reported as empty instead of crashing`() {
        val (store, file) = storeIn()
        file.writeText("{ this is not json")

        assertNull(store.load())
    }

    @Test
    fun `unknown fields in the file are ignored`() {
        val (store, file) = storeIn()
        file.writeText(
            """{"entries":[{"id":"a","category":"QUIZ","prompt":"P","answer":"A","extra":1}],""" +
                """"songs":[],"futureField":"ignored"}""",
        )

        val loaded = store.load()
        assertNotNull(loaded)
        assertEquals("P", loaded!!.entries.single().prompt)
    }

    @Test
    fun `save creates missing parent directories`() {
        val file = File(folder.root, "nested/deeper/library.json")
        val store = LibraryStore(file)

        assertTrue(store.save(LibraryData(songs = listOf(Song("u", "t")))))
        assertTrue(file.exists())
        assertEquals(listOf(Song("u", "t")), store.load()?.songs)
    }

    @Test
    fun `the answer field defaults to empty when absent`() {
        val (store, file) = storeIn()
        file.writeText("""{"entries":[{"id":"a","category":"PODCAST","prompt":"Thema"}],"songs":[]}""")

        assertEquals("", store.load()!!.entries.single().answer)
    }

    @Test
    fun `an empty file is treated as no data`() {
        val (store, file) = storeIn()
        file.writeText("")

        assertNull(store.load())
    }

    @Test
    fun `load does not create the file as a side effect`() {
        val (store, file) = storeIn()
        store.load()
        assertFalse(file.exists())
    }
}

package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate

class ShiftStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun store(name: String = "shift.json") = ShiftStore(File(folder.root, name))

    @Test
    fun `nothing saved yet reads as null`() {
        assertNull(store().load())
    }

    @Test
    fun `settings round-trip`() {
        val s = store()
        val settings = ShiftSettings(
            person = Person.HAGEN,
            festivalStart = "2026-07-02",
            alarmedShiftIds = listOf("do-spaet"),
        )

        assertTrue(s.save(settings))
        assertEquals(settings, s.load())
    }

    @Test
    fun `the festival date parses back to a LocalDate`() {
        val settings = ShiftSettings(festivalStart = "2026-07-02")
        assertEquals(LocalDate.of(2026, 7, 2), settings.festivalStartDate)
    }

    @Test
    fun `an unparseable date reads as null instead of throwing`() {
        assertNull(ShiftSettings(festivalStart = "nicht-ein-datum").festivalStartDate)
        assertNull(ShiftSettings(festivalStart = null).festivalStartDate)
    }

    @Test
    fun `a corrupt file reads as null`() {
        val file = File(folder.root, "broken.json")
        file.writeText("}{")

        assertNull(ShiftStore(file).load())
    }

    @Test
    fun `defaults fill in for a minimal file`() {
        val file = File(folder.root, "minimal.json")
        file.writeText("{}")

        val loaded = ShiftStore(file).load()
        assertEquals(ShiftSettings(), loaded)
    }
}

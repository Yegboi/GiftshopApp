package com.example.showbox.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class ShiftPlanTest {

    /** Any date works: the rota is defined by day offsets, not by weekday. */
    private val start: LocalDate = LocalDate.of(2026, 7, 2)

    private fun at(dayOffset: Long, hour: Int, minute: Int = 0): LocalDateTime =
        start.plusDays(dayOffset).atTime(hour, minute)

    @Test
    fun `rota matches the plan`() {
        fun idsFor(person: Person) = ShiftPlan.shiftsFor(person).map { it.id }.sorted()

        assertEquals(listOf("do-frueh", "fr-spaet", "sa-frueh"), idsFor(Person.BENNI))
        assertEquals(listOf("do-frueh", "fr-frueh", "sa-spaet"), idsFor(Person.JANNA))
        assertEquals(listOf("fr-frueh", "sa-spaet", "so-frueh"), idsFor(Person.JANA))
        assertEquals(listOf("do-spaet", "fr-spaet", "sa-frueh"), idsFor(Person.HAGEN))
        assertEquals(listOf("do-spaet", "so-frueh"), idsFor(Person.GIFTI))
    }

    @Test
    fun `Jana and Janna are different people with different rotas`() {
        assertTrue(Person.JANA != Person.JANNA)
        assertTrue(ShiftPlan.shiftsFor(Person.JANA) != ShiftPlan.shiftsFor(Person.JANNA))
    }

    @Test
    fun `every shift is staffed by exactly two people`() {
        assertTrue(ShiftPlan.shifts.all { it.people.size == 2 })
    }

    @Test
    fun `counts down to the first shift before the festival`() {
        val status = ShiftPlan.statusAt(Person.BENNI, start, at(0, 10, 0))

        assertTrue(status is ShiftStatus.Upcoming)
        status as ShiftStatus.Upcoming
        assertEquals("do-frueh", status.instance.shift.id)
        assertEquals(Duration.ofHours(2), status.remaining)
    }

    @Test
    fun `switches to running the moment a shift starts`() {
        val status = ShiftPlan.statusAt(Person.BENNI, start, at(0, 12, 0))

        assertTrue(status is ShiftStatus.Running)
        status as ShiftStatus.Running
        assertEquals("do-frueh", status.instance.shift.id)
        assertEquals(Duration.ofHours(7), status.remaining)
    }

    @Test
    fun `counts down the remaining time inside a shift`() {
        val status = ShiftPlan.statusAt(Person.BENNI, start, at(0, 17, 30))

        assertTrue(status is ShiftStatus.Running)
        assertEquals(Duration.ofMinutes(90), (status as ShiftStatus.Running).remaining)
    }

    @Test
    fun `the end of a shift is no longer running`() {
        val status = ShiftPlan.statusAt(Person.BENNI, start, at(0, 19, 0))

        assertTrue(status is ShiftStatus.Upcoming)
        assertEquals("fr-spaet", (status as ShiftStatus.Upcoming).instance.shift.id)
    }

    @Test
    fun `the thursday late shift runs past midnight into friday`() {
        val late = ShiftPlan.shifts.single { it.id == "do-spaet" }.instanceOn(start)

        assertEquals(start.atTime(19, 0), late.start)
        assertEquals(start.plusDays(1).atTime(2, 0), late.end)
        assertTrue(late.shift.endApprox)
    }

    @Test
    fun `hagen is still working after midnight on friday`() {
        val status = ShiftPlan.statusAt(Person.HAGEN, start, at(1, 1, 0))

        assertTrue(status is ShiftStatus.Running)
        status as ShiftStatus.Running
        assertEquals("do-spaet", status.instance.shift.id)
        assertEquals(Duration.ofHours(1), status.remaining)
    }

    @Test
    fun `reports all done after the last shift`() {
        assertEquals(ShiftStatus.AllDone, ShiftPlan.statusAt(Person.GIFTI, start, at(3, 12, 0)))
        assertEquals(ShiftStatus.AllDone, ShiftPlan.statusAt(Person.BENNI, start, at(5, 9, 0)))
    }

    @Test
    fun `endedBetween reports a shift end inside the window`() {
        val ended = ShiftPlan.endedBetween(
            person = Person.BENNI,
            festivalStart = start,
            since = at(0, 18, 59),
            now = at(0, 19, 0),
        )

        assertEquals(listOf("do-frueh"), ended.map { it.shift.id })
    }

    @Test
    fun `endedBetween excludes the boundary it already reported`() {
        val ended = ShiftPlan.endedBetween(
            person = Person.BENNI,
            festivalStart = start,
            since = at(0, 19, 0),
            now = at(0, 19, 1),
        )

        assertTrue(ended.isEmpty())
    }

    @Test
    fun `endedBetween finds nothing while a shift is still running`() {
        val ended = ShiftPlan.endedBetween(Person.BENNI, start, at(0, 14, 0), at(0, 15, 0))

        assertTrue(ended.isEmpty())
    }

    @Test
    fun `shift instances are ordered by start time`() {
        val starts = ShiftPlan.instancesFor(Person.JANA, start).map { it.start }

        assertEquals(starts.sorted(), starts)
    }

    @Test
    fun `time labels read as on the plan`() {
        val labels = ShiftPlan.shifts.associate { it.id to it.timeLabel }

        assertEquals("12:00 – 19:00", labels["do-frueh"])
        assertEquals("19:00 – ca. 02:00", labels["do-spaet"])
        assertEquals("08:00 – 12:00", labels["so-frueh"])
    }
}

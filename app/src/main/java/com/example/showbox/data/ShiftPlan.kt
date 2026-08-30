package com.example.showbox.data

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/** The people on the kiosk rota. Jana and Janna are two different people. */
@Serializable
enum class Person(val displayName: String) {
    BENNI("Benni"),
    JANNA("Janna"),
    JANA("Jana"),
    HAGEN("Hagen"),
    GIFTI("Gifti"),
}

/**
 * One slot of the rota, relative to the festival's first day.
 *
 * [endMinute] may exceed 24 h for a shift running past midnight — the
 * Thursday late shift ends at 02:00 the next morning, i.e. minute 1560.
 */
data class Shift(
    val id: String,
    val dayOffset: Int,
    val dayName: String,
    val startMinute: Int,
    val endMinute: Int,
    val people: Set<Person>,
    val endApprox: Boolean = false,
    val note: String = "",
) {
    val timeLabel: String
        get() = "${clock(startMinute)} – ${if (endApprox) "ca. " else ""}${clock(endMinute)}"

    fun instanceOn(festivalStart: LocalDate): ShiftInstance {
        val midnight = festivalStart.plusDays(dayOffset.toLong()).atStartOfDay()
        return ShiftInstance(
            shift = this,
            start = midnight.plusMinutes(startMinute.toLong()),
            end = midnight.plusMinutes(endMinute.toLong()),
        )
    }
}

/** A [Shift] pinned to concrete dates. */
data class ShiftInstance(
    val shift: Shift,
    val start: LocalDateTime,
    val end: LocalDateTime,
)

/** Where a person stands relative to their rota right now. */
sealed interface ShiftStatus {
    /** The shift has not started yet; [remaining] counts down to its start. */
    data class Upcoming(val instance: ShiftInstance, val remaining: Duration) : ShiftStatus

    /** The shift is running; [remaining] counts down to its end. */
    data class Running(val instance: ShiftInstance, val remaining: Duration) : ShiftStatus

    /** No shifts left. */
    data object AllDone : ShiftStatus
}

object ShiftPlan {

    const val FESTIVAL_NAME = "Wasted in Jarmen"
    const val VENUE = "Festivalkiosk"

    /** Message shown and announced when a shift ends. */
    const val SHIFT_OVER_MESSAGE = "Zeit für lecker Bierchen!"

    val shifts: List<Shift> = listOf(
        Shift(
            id = "do-frueh", dayOffset = 0, dayName = "Donnerstag",
            startMinute = 12 * 60, endMinute = 19 * 60,
            people = setOf(Person.BENNI, Person.JANNA),
        ),
        Shift(
            id = "do-spaet", dayOffset = 0, dayName = "Donnerstag",
            startMinute = 19 * 60, endMinute = 26 * 60,
            people = setOf(Person.HAGEN, Person.GIFTI),
            endApprox = true,
            note = "Gifti (DJ Team Ost)",
        ),
        Shift(
            id = "fr-frueh", dayOffset = 1, dayName = "Freitag",
            startMinute = 8 * 60, endMinute = 15 * 60,
            people = setOf(Person.JANA, Person.JANNA),
        ),
        Shift(
            id = "fr-spaet", dayOffset = 1, dayName = "Freitag",
            startMinute = 15 * 60, endMinute = 22 * 60,
            people = setOf(Person.BENNI, Person.HAGEN),
        ),
        Shift(
            id = "sa-frueh", dayOffset = 2, dayName = "Samstag",
            startMinute = 8 * 60, endMinute = 15 * 60,
            people = setOf(Person.BENNI, Person.HAGEN),
        ),
        Shift(
            id = "sa-spaet", dayOffset = 2, dayName = "Samstag",
            startMinute = 15 * 60, endMinute = 22 * 60,
            people = setOf(Person.JANA, Person.JANNA),
        ),
        Shift(
            id = "so-frueh", dayOffset = 3, dayName = "Sonntag",
            startMinute = 8 * 60, endMinute = 12 * 60,
            people = setOf(Person.GIFTI, Person.JANA),
        ),
    )

    fun shiftsFor(person: Person): List<Shift> = shifts.filter { person in it.people }

    fun instancesFor(person: Person, festivalStart: LocalDate): List<ShiftInstance> =
        shiftsFor(person).map { it.instanceOn(festivalStart) }.sortedBy { it.start }

    /**
     * A running shift wins over an upcoming one, so the countdown switches to
     * "how much longer" the moment a shift starts.
     */
    fun statusAt(person: Person, festivalStart: LocalDate, now: LocalDateTime): ShiftStatus {
        val instances = instancesFor(person, festivalStart)

        instances.firstOrNull { !now.isBefore(it.start) && now.isBefore(it.end) }?.let {
            return ShiftStatus.Running(it, Duration.between(now, it.end))
        }
        instances.firstOrNull { now.isBefore(it.start) }?.let {
            return ShiftStatus.Upcoming(it, Duration.between(now, it.start))
        }
        return ShiftStatus.AllDone
    }

    /** Shifts that ended between [since] and [now] — these are due an alarm. */
    fun endedBetween(
        person: Person,
        festivalStart: LocalDate,
        since: LocalDateTime,
        now: LocalDateTime,
    ): List<ShiftInstance> = instancesFor(person, festivalStart)
        .filter { it.end.isAfter(since) && !it.end.isAfter(now) }
}

/** Renders a minute-of-day that may run past midnight, e.g. 1560 -> `02:00`. */
fun clock(minute: Int): String {
    val minutesPerDay = 24 * 60
    val wrapped = ((minute % minutesPerDay) + minutesPerDay) % minutesPerDay
    return String.format(Locale.GERMAN, "%02d:%02d", wrapped / 60, wrapped % 60)
}

/** Formats a countdown as `HH:MM:SS`, prefixed with whole days when there are any. */
fun formatCountdown(duration: Duration): String {
    val total = duration.seconds.coerceAtLeast(0)
    val days = total / 86_400
    val clockPart = String.format(
        Locale.GERMAN,
        "%02d:%02d:%02d",
        (total % 86_400) / 3600,
        (total % 3600) / 60,
        total % 60,
    )
    return when (days) {
        0L -> clockPart
        1L -> "1 Tag, $clockPart"
        else -> "$days Tage, $clockPart"
    }
}

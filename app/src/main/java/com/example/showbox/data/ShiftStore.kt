package com.example.showbox.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

/** Which person the phone belongs to, and when the festival starts. */
@kotlinx.serialization.Serializable
data class ShiftSettings(
    val person: Person? = null,
    /** ISO-8601 date of the festival's Thursday. */
    val festivalStart: String? = null,
    /** Shift ids whose end alarm already fired, so it does not repeat. */
    val alarmedShiftIds: List<String> = emptyList(),
) {
    val festivalStartDate: LocalDate?
        get() = festivalStart?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

/** Same JSON-on-disk approach as [LibraryStore], kept in its own file. */
class ShiftStore(private val file: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun load(): ShiftSettings? {
        if (!file.exists()) return null
        return try {
            json.decodeFromString<ShiftSettings>(file.readText())
        } catch (e: Exception) {
            null
        }
    }

    fun save(settings: ShiftSettings): Boolean = try {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(settings))
        true
    } catch (e: Exception) {
        false
    }
}

/** The next Thursday on or after [today] — the day the rota starts. */
fun defaultFestivalStart(today: LocalDate): LocalDate {
    val offset = (DayOfWeek.THURSDAY.value - today.dayOfWeek.value + 7) % 7
    return today.plusDays(offset.toLong())
}

package com.example.showbox.data

import kotlinx.serialization.Serializable

/**
 * The four question/topic sections. They differ only in whether an entry
 * carries an answer, so one model and one screen serve all of them.
 */
@Serializable
enum class Category(
    /** Shown as the screen title. */
    val label: String,
    /** Whether entries in this category carry an answer. */
    val hasAnswer: Boolean,
    /** What a new entry is called in the input field. */
    val promptLabel: String,
) {
    QUIZ("Quiz", true, "Frage"),
    SPEED_DATING("Speed Dating", false, "Frage"),
    ESTIMATION("Schätzfragen", true, "Schätzfrage"),
    PODCAST("Podcast-Themen", false, "Thema"),
}

/** A question, a speed-dating prompt or a podcast topic. */
@Serializable
data class Entry(
    val id: String,
    val category: Category,
    val prompt: String,
    /** Empty for categories where [Category.hasAnswer] is false. */
    val answer: String = "",
)

/** An audio file the user picked, referenced by its persisted content URI. */
@Serializable
data class Song(
    val uri: String,
    val title: String,
)

/** Everything the app persists. */
@Serializable
data class LibraryData(
    val entries: List<Entry> = emptyList(),
    val songs: List<Song> = emptyList(),
)

package com.example.showbox.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Reads and writes the library as JSON on disk. Takes a [File] rather than a
 * Context so the storage logic can be tested on the JVM.
 */
class LibraryStore(private val file: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /** Returns null when nothing usable is stored yet, so callers can seed defaults. */
    fun load(): LibraryData? {
        if (!file.exists()) return null
        return try {
            json.decodeFromString<LibraryData>(file.readText())
        } catch (e: Exception) {
            // A truncated or hand-edited file should not brick the app.
            null
        }
    }

    /** Returns true when the data reached disk. */
    fun save(data: LibraryData): Boolean = try {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(data))
        true
    } catch (e: Exception) {
        false
    }
}

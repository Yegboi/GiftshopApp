package com.example.steamfun.data

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

/**
 * Keeps Steam's catalogue of appids on disk so it is downloaded once rather
 * than every launch. Stored as raw ints — a few hundred thousand of them are
 * about a megabyte, against tens of megabytes for the JSON they came from.
 */
class AppIdCache(private val file: File) {

    /** Returns the cached ids, or null when absent, stale or unreadable. */
    fun load(maxAgeMillis: Long): IntArray? {
        if (!file.exists()) return null
        if (System.currentTimeMillis() - file.lastModified() > maxAgeMillis) return null
        return try {
            DataInputStream(file.inputStream().buffered()).use { stream ->
                if (stream.readInt() != MAGIC) return null
                val count = stream.readInt()
                if (count <= 0 || count > MAX_COUNT) return null
                IntArray(count) { stream.readInt() }
            }
        } catch (e: Exception) {
            // Truncated by a killed process, or written by an older format.
            null
        }
    }

    fun save(ids: IntArray): Boolean = try {
        file.parentFile?.mkdirs()
        DataOutputStream(file.outputStream().buffered()).use { stream ->
            stream.writeInt(MAGIC)
            stream.writeInt(ids.size)
            ids.forEach(stream::writeInt)
        }
        true
    } catch (e: Exception) {
        false
    }

    private companion object {
        /** Guards against reading a file that is not ours. */
        const val MAGIC = 0x53544D31
        const val MAX_COUNT = 2_000_000
    }
}
